package unidue.rc.ui.pages.roles;


import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Checkbox;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.slf4j.Logger;
import se.unbound.tapestry.breadcrumbs.BreadCrumb;
import unidue.rc.dao.ActionDAO;
import unidue.rc.dao.CommitException;
import unidue.rc.dao.DeleteException;
import unidue.rc.dao.PermissionDAO;
import unidue.rc.dao.RoleDAO;
import unidue.rc.model.Action;
import unidue.rc.model.ActionDefinition;
import unidue.rc.model.PermissionDefinition;
import unidue.rc.model.Role;
import unidue.rc.security.RequiresActionPermission;
import unidue.rc.ui.ProtectedPage;
import unidue.rc.ui.selectmodel.ActionSelectModel;
import unidue.rc.ui.valueencoder.ActionValueEncoder;
import unidue.rc.ui.valueencoder.RoleValueEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main page to manage {@link Role} objects.
 *
 * @author Nils Verheyen
 */
@BreadCrumb(titleKey = "roles")
@Import(library = {"context:js/roles.index.js"})
@ProtectedPage
public class Index {

    @Inject
    private Logger log;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private ActionDAO actionDAO;

    @Inject
    private PermissionDAO permissionDAO;

    /* Fields in Zone to select a role and actions to add */

    @Property
    @Persist
    private SelectModel roleSelectModel;

    @Property
    @Persist
    private ActionSelectModel unselectedActionsModel;

    @Inject
    SelectModelFactory selectModelFactory;

    @Property
    @Persist
    private Action selectedAction;

    @Property
    /*
     * use flash persistence here as the selected role is set through select
     * list in ui in every request
     */
    @Persist
    private Role selectedRole;

    /* Fields in zone to remove actions of role and to toggle instance binding */
    @Property
    private PermissionDefinition permissionDefinition;

    // Working fields

    private boolean inFormSubmission;

    private List<PermissionDefinition> permissionsToToggle;

    @InjectComponent
    private Checkbox bindCheckbox;

    /* Page components and resources */

    @InjectComponent
    private Form availableActionsForm;

    @InjectComponent
    private Form permissionDefinitionForm;

    @InjectComponent("availableActions")
    private Field availableActionsComponent;

    @InjectComponent
    private Zone selectedActionsZone;

    @InjectComponent
    private Zone availableActionsZone;

    @Inject
    private Messages messages;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    @Inject
    private Request request;

    @Persist(PersistenceConstants.FLASH)
    @Property
    private String permissionSavedMessage;

    @SetupRender
    @RequiresActionPermission(ActionDefinition.EDIT_ROLES)
    public void onSetupRender() {

        List<Role> roles = roleDAO.getRoles();

        selectedRole = null;
        selectedAction = null;

        roleSelectModel = selectModelFactory.create(sortRoles(roles),
                Role.NAME_PROPERTY);
        unselectedActionsModel = new ActionSelectModel(Collections.EMPTY_LIST, messages);
    }

    /**
     * Sorts the list of roles by its name.
     *
     * @param result
     * @return
     */
    private List<Role> sortRoles(List<Role> result) {
        return result
                .stream()
                .sorted((r1, r2) -> r1.getName().compareTo(r2.getName()))
                .collect(Collectors.toList());
    }

    @OnEvent(value = "add")
    void onAddAction() {
        if (selectedAction != null) {
            log.debug("selected action " + selectedAction.getResource() + "." + selectedAction.getName());

            PermissionDefinition permissionDefinition = new PermissionDefinition();
            permissionDefinition.setIsInstanceBound(Boolean.TRUE);
            permissionDefinition.setAction(selectedAction);
            permissionDefinition.setRole(selectedRole);
            try {
                roleDAO.create(permissionDefinition);
            } catch (CommitException e) {
                availableActionsForm.recordError(availableActionsComponent, messages.get("error.msg.could.not.bind.action"));
            }
            unselectedActionsModel.removeAction(selectedAction);
            selectedAction = null;
        }
        renderZones();
    }

    @OnEvent(component = "removeActionLink")
    void onRemovePermissionDefinition(PermissionDefinition definition) {
        String permissionLabel = getLabel(definition.getAction());

        log.debug("removing definition " + permissionLabel);
        try {
            permissionDAO.delete(definition);
        } catch (DeleteException e) {
            permissionDefinitionForm.recordError(messages.format("error.msg.could.not.delete.permission", permissionLabel));
        }
        renderZones();
    }

    @OnEvent(value = "deleteRole")
    Object onDeleteRole() {
        try {
            roleDAO.delete(selectedRole);
        } catch (DeleteException e) {
            availableActionsForm.recordError(messages.format("error.msg.could.not.delete.role",
                    selectedRole.getName()));
        }
        return Index.class;
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "roles")
    void onRoleSelected(Role selectedRole) {

        // role is null when placeholder is selected
        if (selectedRole != null) {

            List<Action> actions = actionDAO.getActions();
            List<Action> unselectedActions = actionDAO
                    .getUnrelatedActions(selectedRole);

            for(Action test : unselectedActions) {
                log.debug("UnselectedAction for selectedRole "+selectedRole.getName() +":"+selectedRole.getId()+" Action is " +test);
            }


             unselectedActions = actionDAO
                    .getUnrelatedActions(selectedRole)
                    .stream()
                    .sorted((a1, a2) -> getLabel(a1).compareTo(getLabel(a2)))
                    .collect(Collectors.toList());

            unselectedActionsModel = new ActionSelectModel(unselectedActions, messages);
            this.selectedRole = selectedRole;
        }
        renderZones();
    }

    @OnEvent(value = EventConstants.VALUE_CHANGED, component = "availableActions")
    void onActionSelected(Action action) {
        this.selectedAction = action;
        renderZones();
    }

    @OnEvent(EventConstants.PREPARE_FOR_SUBMIT)
    void onPrepareForSubmit() {

        // user has selected books to add. begin with submission
        inFormSubmission = true;
        permissionsToToggle = new ArrayList<>();
    }

    @OnEvent(value = EventConstants.VALIDATE, component = "permissionDefinitionForm")
    public void onValidatePermissionDefinitions() {
        if (permissionDefinitionForm.getHasErrors())
            return;

        for (PermissionDefinition definition : permissionsToToggle) {
            definition.setIsInstanceBound(!definition.isIsInstanceBound());
            try {
                permissionDAO.update(definition);
            } catch (CommitException e) {
                permissionDefinitionForm.recordError(bindCheckbox, messages.format("error.msg.could.not.commit.permission", definition));
            }
        }
        renderZones();
    }

    @OnEvent(value = EventConstants.SUCCESS, component = "permissionDefinitionForm")
    Object onDefinitionsUpdated() {
        permissionSavedMessage = messages.get("permissions.updated");
        return this;
    }

    public List<PermissionDefinition> getPermissionDefinitions() {
        return selectedRole != null
                ? selectedRole.getPermissionDefinitions()
                    .stream()
                    .sorted((p1, p2) -> getLabel(p1.getAction()).compareTo(getLabel(p2.getAction())))
                    .collect(Collectors.toList())
                : Collections.EMPTY_LIST;
    }

    public String getActionLabel() {
        return getLabel(permissionDefinition.getAction());
    }

    private String getLabel(Action action) {
        return messages.get(action.getResource() + "." + action.getName());
    }

    public void setBind(boolean bind) {
        if (inFormSubmission && permissionDefinition.isIsInstanceBound() != bind) {
            permissionsToToggle.add(permissionDefinition);
        }
    }

    public boolean getBind() {
        return permissionDefinition.isIsInstanceBound();
    }

    private void renderZones() {
        if (request.isXHR()) {
            permissionSavedMessage = null;
            ajaxResponseRenderer
                    .addRender(selectedActionsZone)
                    .addRender(availableActionsZone);
        }
    }

    public ValueEncoder<Role> getRoleValueEncoder() {
        return new RoleValueEncoder(roleDAO);
    }

    public ValueEncoder<Action> getActionEncoder() {
        return new ActionValueEncoder(actionDAO);
    }
}

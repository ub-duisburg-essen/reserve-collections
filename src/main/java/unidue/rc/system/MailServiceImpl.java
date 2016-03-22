package unidue.rc.system;

/*
 * #%L
 * Semesterapparate
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 - 2015 Universitaet Duisburg Essen
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import miless.model.MCRCategory;
import miless.model.User;
import org.apache.cayenne.BaseContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.di.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unidue.rc.dao.*;
import unidue.rc.model.*;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by nils on 24.06.15.
 */
public class MailServiceImpl implements MailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailServiceImpl.class);

    private static final String ORIGIN_DIVIDER = " &raquo; ";
    private static final String AUTHOR_DIVIDER = ", ";

    @Inject
    private BaseURLService urlService;

    @Inject
    private SystemConfigurationService config;

    @Inject
    private MailDAO mailDAO;

    @Inject
    private RoleDAO roleDAO;

    @Inject
    private OriginDAO originDAO;

    @Inject
    private MCRCategoryDAO categoryDAO;

    @Inject
    private ParticipationDAO participationDAO;

    @Inject
    private UserDAO userDAO;

    @Override
    public void sendMail(Mail mail) throws EmailException {

        if (!config.getBoolean("mail.send"))
            return;

        if (mail.getNumTries() >= config.getInt("mail.max.tries")) {
            return;
        }

        String from = mail.getFrom();
        String subject = mail.getSubject();
        String mailBody = mail.getMailBody();
        String textMailBody = mail.getTextMailBody();
        List<String> recipients = mail.getNodes(MailNodeType.RECIPIENT);
        List<String> replyToAddresses = mail.getNodes(MailNodeType.REPLY_TO);
        List<String> ccAddresses = mail.getNodes(MailNodeType.CC);
        List<String> bccAddresses = mail.getNodes(MailNodeType.BCC);
        mail.setNumTries(mail.getNumTries() + 1);

        LOG.info("trying to send from " + from + " to " + recipients);

        // read config
        String smtpHost = config.getString("mail.smtp.host");
        String smtpUser = config.getString("mail.smtp.user");
        String smtpPassword = config.getString("mail.smtp.password");
        Integer smtpPort = config.getInt("mail.smtp.port", 25);
        Boolean startTLSEnable = config.getBoolean("mail.smtp.start.tls", Boolean.FALSE);
        Boolean useDebug = config.getBoolean("mail.mail.debug", Boolean.FALSE);

        // validate settings
        StringBuilder errMsg = new StringBuilder();
        if (smtpHost == null || smtpHost.isEmpty())
            errMsg.append("smtp host not specified. ");
        if (smtpUser == null || smtpUser.isEmpty())
            errMsg.append("user not specified. ");
        if (smtpPassword == null || smtpPassword.isEmpty())
            errMsg.append("password not specified. ");
        if (from == null || from.isEmpty())
            errMsg.append("from not specified. ");
        if (recipients == null || recipients.isEmpty())
            errMsg.append("recipients not specified. ");
        if (mailBody == null || mailBody.isEmpty())
            errMsg.append("mailBody not specified. ");
        if (subject == null || subject.isEmpty())
            LOG.warn("no subject specified for mail from " + from + " to " + recipients);
        if (errMsg.length() > 0)
            throw new IllegalArgumentException(errMsg.toString());

        // build email
        HtmlEmail email = new HtmlEmail();
        email.setCharset(config.getString("mail.charset"));
        email.setHostName(smtpHost);
        email.setSmtpPort(smtpPort);
        email.setAuthentication(smtpUser, smtpPassword);
        email.setSSLOnConnect(startTLSEnable);
        email.setFrom(from);
        email.setSubject(subject);
        email.setDebug(useDebug);
        if (StringUtils.isNotBlank(textMailBody))
            email.setTextMsg(textMailBody);

        // set recipients
        email.setTo(buildAddressList(recipients));
        List<InternetAddress> cc = buildAddressList(ccAddresses);
        if (!cc.isEmpty())
            email.setCc(cc);
        List<InternetAddress> bcc = buildAddressList(bccAddresses);
        if (!bcc.isEmpty())
            email.setBcc(bcc);
        List<InternetAddress> replyTo = buildAddressList(replyToAddresses);
        if (!replyTo.isEmpty())
            email.setReplyTo(replyTo);

        email.setHtmlMsg(mailBody);

        try {
            // save mail in backend for later use
            if (mail.getPersistenceState() == PersistenceState.NEW)
                mailDAO.create(mail);

            // send mail
            email.send();

            // update mail after successful send
            mail.setSend(true);
            mail.setSendDate(new Date());
        } catch (CommitException e) {
            LOG.error("could not commit mail", e);
        } catch (EmailException e) {
            mail.setSend(false);
            mail.setSendDate(null);
            throw e;
        } finally {
            try {
                mailDAO.update(mail);
            } catch (CommitException e) {
                LOG.error("could not update mail", e);
            }
        }

        LOG.info("mail send from " + from + " to " + recipients);
    }

    public String buildSubject(Entry entry, String bookType, String authors) {

        ReserveCollection collection = entry.getReserveCollection();

        StringBuilder subject = new StringBuilder();
        subject.append(collection.getLibraryLocation().getName());
        subject.append(" - ");
        subject.append(collection.getNumber().getNumber());
        subject.append(" - ");
        subject.append(entry.getId());
        subject.append(": ");
        subject.append(bookType);
        subject.append(" [");
        subject.append(authors);
        subject.append(']');
        return subject.toString();
    }

    public String buildOrigin(ReserveCollection collection) {

        Integer originId = collection.getOriginId();
        if (originId != null) {
            MCRCategory category = categoryDAO.getCategoryById(originId);
            return buildOrigin(category);
        }
        return StringUtils.EMPTY;
    }

    public String buildOrigin(User user) {

        String originId = user.getOrigin();
        if (originId != null) {
            MCRCategory category = originDAO.getOrigin(originId);
            return buildOrigin(category);
        }
        return StringUtils.EMPTY;
    }

    private String buildOrigin(MCRCategory category) {

        List<MCRCategory> categories = new ArrayList<>();
        // add every category except the root
        do {
            categories.add(0, category);
            category = category.getParentCategory();
        } while (category != null && category.getParentCategory() != null);
        Object[] categoryLabels = categories.stream()
                .map(c -> originDAO.getOriginLabel(Locale.GERMAN, c.getInternalId()))
                .toArray();
        String origin = StringUtils.join(categoryLabels, ORIGIN_DIVIDER);
        return origin;
    }

    public String buildAuthors(ReserveCollection collection) {

        List<Participation> participations = participationDAO.getActiveParticipations(roleDAO.getRole(DefaultRole.DOCENT), collection);
        Object[] docentNames = participations.stream()
                .map(participation -> userDAO.getUserById(participation.getUserId()))
                .filter(user -> user != null)
                .map(user -> user.getRealname())
                .toArray();
        String authors = StringUtils.join(docentNames, AUTHOR_DIVIDER);
        return authors;
    }

    public String createCollectionLink(ReserveCollection collection) {
        return urlService.getViewCollectionURL(collection);
    }

    @Override
    public String createEntryLink(Entry entry) {
        return createCollectionLink(entry.getReserveCollection()) + "#" + entry.getId();
    }

    /**
     * Builds a list of email addresses for target list. If an address could not
     *
     * @param addresses
     * @return
     */
    private static List<InternetAddress> buildAddressList(List<String> addresses) {
        return addresses.stream()
                .map(address -> {
                    try {
                        return buildAddress(address);
                    } catch (AddressException e) {
                        LOG.error("could not build email from " + address);
                        return null;
                    }
                })
                .filter(address -> address != null)
                .collect(Collectors.toList());
    }

    /**
     * Builds email address from a string. The string may be a single email
     * address or a combination of a personal name and address, like "John Doe"
     * <john@doe.com>
     *
     * @throws AddressException thrown if the parse of target address failed
     */
    private static InternetAddress buildAddress(String address) throws AddressException {

        if (StringUtils.isEmpty(address))
            return null;

        if (!address.endsWith(">")) {
            return new InternetAddress(address.trim());
        }

        String name = address.substring(0, address.lastIndexOf("<")).trim();
        String addr = address.substring(address.lastIndexOf("<") + 1, address.length() - 1).trim();

        if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }

        try {

            return new InternetAddress(addr, name);
        } catch (UnsupportedEncodingException e) {
            LOG.error("could not encode address: " + address + " - " + e.getMessage());
            return null;
        }
    }

    public MailBuilder builder(String templateName) {
        return new MailBuilder(templateName);
    }

    public static class MailBuilder {

        private final Mail mail;
        private final List<MailNode> nodes;
        private final String templateName;
        private String textTemplateName;

        public MailBuilder(String templateName) {
            this.mail = new Mail();
            this.nodes = new ArrayList<>();
            this.templateName = templateName;
        }

        public MailBuilder from(String from) {
            mail.setFrom(from);
            return this;
        }

        public MailBuilder subject(String subject) {
            mail.setSubject(subject);
            return this;
        }

        public MailBuilder context(VelocityContext context) throws ResourceNotFoundException, ParseErrorException, IOException {

            // create template
            mail.setMailBody(merge(templateName, context));

            if (textTemplateName != null) {
                mail.setTextMailBody(merge(textTemplateName, context));
            }
            return this;
        }

        private String merge(String templateFile, VelocityContext context) throws IOException {
            StringWriter writer = new StringWriter();
            Template template = Velocity.getTemplate(templateFile, "UTF-8");
            template.merge(context, writer);
            String result = writer.toString();
            writer.close();
            return result;
        }

        public MailBuilder addRecipient(String recipient) {
            MailNode node = new MailNode();
            node.setType(MailNodeType.RECIPIENT);
            node.setValue(recipient);
            nodes.add(node);
            return this;
        }

        public MailBuilder addRecipients(String...recipients) {
            for (String recipient : recipients) {

                MailNode mailNode = new MailNode();
                mailNode.setValue(recipient);
                mailNode.setType(MailNodeType.RECIPIENT);
                nodes.add(mailNode);
            }
            return this;
        }

        public MailBuilder addCc(String...cc) {
            for (String s : cc) {

                MailNode node = new MailNode();
                node.setType(MailNodeType.CC);
                node.setValue(s);
                nodes.add(node);
            }
            return this;
        }

        public MailBuilder addBcc(String...bcc) {
            for (String s : bcc) {

                MailNode node = new MailNode();
                node.setType(MailNodeType.BCC);
                node.setValue(s);
                nodes.add(node);
            }
            return this;
        }

        public MailBuilder addReplyTo(String replyTo) {
            MailNode node = new MailNode();
            node.setType(MailNodeType.REPLY_TO);
            node.setValue(replyTo);
            nodes.add(node);
            return this;
        }

        public MailBuilder textTemplateName(String textTemplateName) {
            this.textTemplateName = textTemplateName;
            return this;
        }

        public Mail create() throws IOException {

            if (mail.getMailBody() == null)
                context(new VelocityContext());

            ObjectContext context = BaseContext.getThreadObjectContext();
            context.registerNewObject(mail);
            nodes.forEach(node -> {
                context.registerNewObject(node);
                node.setMail(mail);
            });
            return mail;
        }
    }
}

package unidue.rc.model;


import org.apache.commons.lang3.builder.ToStringBuilder;
import unidue.rc.model.auto._Mail;

import java.util.List;
import java.util.stream.Collectors;

public class Mail extends _Mail implements IntPrimaryKey {

    @Override
    public Integer getId() {
        return CayenneUtils.getID(this, ID_PK_COLUMN);
    }

    public List<String> getNodes(MailNodeType nodeType) {
        return getNodes().stream()
                .filter(node -> node.getType().equals(nodeType))
                .map(node -> node.getValue())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(ID_PK_COLUMN, getId())
                .append(FROM_PROPERTY, getFrom())
                .append(SUBJECT_PROPERTY, getSubject())
                .append(NUM_TRIES_PROPERTY, getNumTries())
                .toString();
    }
}

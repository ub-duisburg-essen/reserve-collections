package unidue.rc.model;


/**
 * Created by nils on 06.08.15.
 */
public interface CollectionVisitable {

    void accept(CollectionVisitor visitor);
}

package unidue.rc.model.legacy;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by nils on 12.08.15.
 */
@Root(name="slots", strict = false)
public class Slots {

    @ElementList(inline = true)
    private List<Slot> slots;

    public List<Slot> getSlots() {
        return slots;
    }
}

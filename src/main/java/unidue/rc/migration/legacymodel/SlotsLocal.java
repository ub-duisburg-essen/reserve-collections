package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "slots", strict = false)
public class SlotsLocal {

    @ElementList(name = "slot", inline = true)
    private List<SlotLocal> slots;

    /**
     * @return the slots
     */
    public List<SlotLocal> getSlots() {
        return slots;
    }

}

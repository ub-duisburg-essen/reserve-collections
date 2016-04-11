package unidue.rc.migration.legacymodel;


import org.simpleframework.xml.strategy.TreeStrategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Value;
import org.simpleframework.xml.stream.NodeMap;

import java.util.Map;

public class DefModsVisitor extends TreeStrategy {

    @Override
    public Value read(Type type, NodeMap node, Map map) throws Exception {
        return null;
    }
}

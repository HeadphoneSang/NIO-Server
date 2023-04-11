package com.chbcraft.internals.components;

import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.entries.PluginEntry;
import com.chbcraft.internals.components.enums.SectionName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class TopologyMachine{
    private final boolean enableDepend;
    TopologyMachine(){
        Configuration prop = FloatSphere.createProperties();
        if(prop!=null){
            Object boolObj = prop.getValueByKey(SectionName.ENABLE_DEPEND_OTHER);
            if(boolObj!=null){
                enableDepend = Boolean.parseBoolean(String.valueOf(boolObj));
            }else
                enableDepend = true;
        }
        else
            enableDepend = true;
    }

    /**
     * 拓扑排序,将插件根据前后顺序进行排序
     *  @param pluginEntries 插件集合
     */
    public void sort(ArrayList<PluginEntry> pluginEntries){
        long start = System.currentTimeMillis();
        if(!enableDepend)
            return;
        LinkedList<PluginNode> rootQueue = new LinkedList<>();
        PluginNode[] graphS = createGraph(pluginEntries);
        for(PluginNode nowNode : graphS)
            if(nowNode.in==0)
                rootQueue.addFirst(nowNode);
        ArrayList<PluginNode> outList = new ArrayList<>();
        while(!rootQueue.isEmpty()) {
            PluginNode nowNode = rootQueue.pollFirst();
            nowNode.outNode();
            outList.add(nowNode);
            for(PluginNode checkNode :nowNode.nextNodes) {
                checkNode.subtract(1);
                if(checkNode.in==0)
                    rootQueue.addFirst(checkNode);
            }
        }
        long spend = (System.currentTimeMillis() - start);
        if(pluginEntries.size()==outList.size()){
            PluginEntry pluginEntry;
            for(int i = 0;i< pluginEntries.size();i++){
                pluginEntry = outList.get(i).pluginEntry;
                pluginEntry.setStartTime(spend);
                pluginEntries.set(i,outList.get(i).pluginEntry);
            }
        }
        else{
            pluginEntries.clear();
            Arrays.stream(graphS).forEach((node) -> { if(!node.isOut){ MessageBox.getLogger().warn("Conflict Plugin-"+node.pluginName); }else pluginEntries.add(node.pluginEntry);});
        }
    }

    /**
     * 将所有的插件容器构建一个有向图
     * @param pluginEntries 所有的插件项集合
     */
    private PluginNode[] createGraph(ArrayList<PluginEntry> pluginEntries){
        int size = pluginEntries.size();
        PluginNode[] graphs = new PluginNode[size];
        for(int i = 0;i<size;i++)
            graphs[i] = new PluginNode(pluginEntries.get(i));
        PluginNode nowNode;
        boolean[] visited = new boolean[size];
        for(int i = 0;i<size;i++){
            nowNode = graphs[i];
            PluginNode checkNode;
            visited[i] = true;
            for(int j = 0;j<size;j++){
                if(visited[j])
                    continue;
                checkNode = graphs[j];
                if(checkNodeIsRely(nowNode,checkNode)){
                    nowNode.addIn(1);//入边加一
                    checkNode.addNextNode(nowNode);//被以来的节点的依赖节点与这个节点引用
                }
            }
            visited[i] = false;
        }
        return graphs;
    }

    /**
     * 查看是否符合入边加一的条件
     * @param nowNode 当前的根节点
     * @param checkNode 正在被检查的节点
     * @return 返回是否满足入边加一
     */
    private boolean checkNodeIsRely(PluginNode nowNode,PluginNode checkNode){
        String nowName = nowNode.pluginName;
        String checkName = checkNode.pluginName;
        boolean flag1 = false;
        /*
        查看当前节点的所有依赖节点,如果依赖节点中含有搜索目前的这个节点,则说明这个被搜索到的节点在这个当前节点有一个入边
         */
        if(nowNode.pluginEntry.getAfterPlugins()!=null)
            for(String str : nowNode.pluginEntry.getAfterPlugins())
                if(str.equals(checkName)){
                    flag1 = true;
                    break;
                }
        boolean flag2 = false;
        /*
        查看当前找到的这个节点的所有要在他后面加载的插件中是否含有当前的节点,如果有则说明当前的节点有这个节点的入边
         */
        if(checkNode.pluginEntry.getBeforePlugins()!=null)
            for(String str : checkNode.pluginEntry.getBeforePlugins())
                if(str.equals(nowName)){
                    flag2 = true;
                    break;
                }
        return flag1||flag2;
    }

    /**
     * 图中的节点
     */
    static class PluginNode{
        private boolean isOut = false;
        /**
         * 这个插件当前依赖几个插件
         */
        private int in;
        /**
         * 这个插件的本体
         */
        private final PluginEntry pluginEntry;
        /**
         * 插件的本名
         */
        private final String pluginName;
        /**
         * 依赖于本插件的插件
         */
        private final ArrayList<PluginNode> nextNodes;

        /**
         * 构建插件的图节点
         * @param pluginEntry 插件本体
         */
        PluginNode(PluginEntry pluginEntry){
            this.pluginEntry = pluginEntry;
            this.pluginName = pluginEntry.getPluginName();
            nextNodes = new ArrayList<>();
        }
        public void outNode(){
            this.isOut = true;
        }
        /**
         * 增加依赖数量
         * @param i 增加的数量
         */
        public void addIn(int i){
            this.in+=i;
        }
        /**
         * 减少依赖数量
         * @param i 减少的数量
         */
        public void subtract(int i){
            this.in-=i;
        }

        /**
         * 增加新的所有依赖于本节点的节点
         * @param node 增加的节点
         */
        public void addNextNode(PluginNode node){
            nextNodes.add(node);
        }
    }
}

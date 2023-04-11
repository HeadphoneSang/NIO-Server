package com.chbcraft.internals.components;

import com.chbcraft.internals.components.entries.config.PluginConfiguration;
import com.chbcraft.internals.components.entries.Section;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import java.io.File;
import java.util.List;

public class YamlConfiguration extends PluginConfiguration {


    /**
     * 将此文件对象转化为String数据
     * @return 返回String数据
     */
    @Override
    public String saveToString(){
        StringBuilder data = new StringBuilder();
        sections.values().forEach((section) ->{
            data.append(castToString(section,0)).append("\n");
        });
        return data.toString();
    }

    /**
     * 把一项键值对转化为字符串
     * @param section 键值对项
     * @param indent 缩进次数
     * @return 返回字符串
     */
    public String castToString(Section section,int indent){
        StringBuilder data = new StringBuilder();
        data.append(getIndent(indent)).append(section.getKey()).append(':').append(' ');
        Object value = section.getValue();
        if(value instanceof List){
            List<Object> list = ConfigurationUtil.castToObjList(value);
            indent++;
            for(Object obj:list){
                if(obj instanceof Section){
                    data.append("\n").append(castToString((Section)obj,indent ));
                }
                else
                    data.append("\n").append(getIndent(indent)).append("-").append(" ").append(obj);
            }
        }
        else{
            data.append(value);
        }
        return data.toString();
    }

    /**
     * 获得字符缩进次数
     * @param indent 所进的次数
     * @return 返回缩进字符串
     */
    public String getIndent(int indent){
        StringBuilder builder = new StringBuilder();
        for(int i = 0;i<indent;i++){
            builder.append("  ");
        }
        return builder.toString();
    }
    public void setFile(File file){
        this.originalFile = file;
    }
}

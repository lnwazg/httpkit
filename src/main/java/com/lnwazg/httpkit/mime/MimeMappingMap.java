package com.lnwazg.httpkit.mime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.kit.describe.D;
import com.lnwazg.kit.xml.xstream.XmlXstreamUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("mapping")
public class MimeMappingMap
{
    @XStreamImplicit
    private List<MimeMapping> list;
    
    /**
     * 直接渲染、非渲染的内容类型
     */
    public static String[] directContentTypes = {"image", "video", "pdf", "text", "audio", "json", "xml", "sql", "javascript"};
    
    public List<MimeMapping> getList()
    {
        return list;
    }
    
    public void setList(List<MimeMapping> list)
    {
        this.list = list;
    }
    
    @Override
    public String toString()
    {
        return "MimeMappingMap [list=" + list + "]";
    }
    
    @XStreamAlias("mime-mapping")
    static class MimeMapping
    {
        @XStreamAsAttribute
        @XStreamAlias("extension")
        String extension;
        
        @XStreamAsAttribute
        @XStreamAlias("mime-type")
        String mimeType;
        
        public String getExtension()
        {
            return extension;
        }
        
        public void setExtension(String extension)
        {
            this.extension = extension;
        }
        
        public String getMimeType()
        {
            return mimeType;
        }
        
        public void setMimeType(String mimeType)
        {
            this.mimeType = mimeType;
        }
        
        @Override
        public String toString()
        {
            return "MimeMapping [extension=" + extension + ", mimeType=" + mimeType + "]";
        }
    }
    
    /**
     * MIME信息映射表
     */
    public static Map<String, String> mimeMap = new HashMap<>();
    
    static
    {
        MimeMappingMap mimeMappingMap = XmlXstreamUtils.fromXml(MimeMappingMap.class.getResourceAsStream("mime_mapping.xml"), MimeMappingMap.class);
        for (MimeMapping mapping : mimeMappingMap.getList())
        {
            if (StringUtils.isNotEmpty(mapping.getExtension()) && StringUtils.isNotEmpty(mapping.getMimeType()))
            {
                mimeMap.put(mapping.getExtension().trim(), mapping.getMimeType().trim());
            }
        }
    }
    
    public static void main(String[] args)
    {
        D.d(mimeMap);
    }
    
}

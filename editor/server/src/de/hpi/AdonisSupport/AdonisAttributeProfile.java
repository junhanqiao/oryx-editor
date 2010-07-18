package de.hpi.AdonisSupport;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmappr.Attribute;
import org.xmappr.Element;
import org.xmappr.Elements;
import org.xmappr.RootElement;

//<!ELEMENT ATTRIBUTEPROFILE ((ATTRIBUTE | RECORD | INTERREF)*)>
//<!ATTLIST ATTRIBUTEPROFILE
//  class   CDATA #REQUIRED
//  name    CDATA #REQUIRED
//  version CDATA #REQUIRED
//  applib  CDATA #REQUIRED
//>

@RootElement("ATTRIBUTEPROFILE")
public class AdonisAttributeProfile extends XMLConvertible{
	
	@Attribute("class")
	protected String aPClass;
	public void setAPClass(String value){ aPClass = value;}
	public String getAPClass(){return aPClass;}
	@Attribute("name")
	protected String name;
	public void setName(String value){ name = value;}
	public String getName(){return name;}
	@Attribute("version")
	protected String version;
	public void setVersion(String value){ version = value;}
	public String getVersion(){return version;}
	@Attribute("applib")
	protected String applib;
	public void setApplib(String value){ applib = value;}
	public String getApplib(){return applib;}

	
	@Elements({
		@Element(name="ATTRIBUTE", targetType=AdonisAttribute.class),
		@Element(name="RECORD", targetType=AdonisRecord.class),
		@Element(name="INTERREF", targetType=AdonisInterref.class)
		
	})
	protected ArrayList<XMLConvertible> children;
	
	public ArrayList<XMLConvertible> getChildren(){
		return children;
	}
	
	public void setChildren(ArrayList<XMLConvertible> list){
		children = list;
	}
	
	public void writeJSONAttributes(JSONObject json) throws JSONException{
		
	}
	public void writeJSONChildren(JSONObject json) throws JSONException{
		json.put("attributeProfile", getChildren());
	}
	
//	public void write(JSONObject json) throws JSONException{
//		super.write(json);
//		for (AdonisBaseObject aBaseObject : getChildren()){
//			aBaseObject.write(json);
//		}
//	}
	@Override
	public void write(JSONObject json) throws JSONException {
//		json.object();
//			json.key("class").value(getAPClass());
//			json.key("name").value(getName());
//			json.key("version").value(getVersion());
//			json.key("applib").value(getApplib());
//			json.key("attributeProfile_children").array();
//			for (AdonisBaseObject aBaseObject : getChildren()){
//				aBaseObject.write(json);
//			}
//			json.endArray();
//		json.endObject();		
	}
}



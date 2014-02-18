/*
This file is part of the pn2sc package.
Copyright (C) 2005 Rik Eshuis (h.eshuis@tm.tue.nl

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.filter.*;
import java.io.*;
import java.util.*;
import java.net.*;


import java.awt.geom.Rectangle2D;


public class ad2grgen {

  public static Document getDocument(String file) throws Exception {      
	    SAXBuilder builder = new SAXBuilder();
	    Document doc = builder.build(file);
	    return doc;
	 }
    

  public static String getNameWithoutNewlines(Element e) {
	String n= e.getAttributeValue("name");
	if (n==null) { 
		return "";
	} else {
		return n.replaceAll("(\\r|\\n)", "").replace("&#10;","");
	}
  }

  public static String mapNode(Element el, final String type, final String pid, Namespace ns) {
	String id = el.getAttributeValue("id", ns);
	String vis = el.getAttributeValue("visibility");	
	StringBuffer out= new StringBuffer("new :"+type);
	out.append("($=\""+id+"\"");
	out.append(",name=\""+getNameWithoutNewlines(el)+"\"");
	out.append(",visibility=Visibility::"+vis);
	out.append(")\n");
	out.append("new @(\""+pid+"\")"+  " - :ownedElement -> @(\"" + id+ "\")\n" );
	return out.toString();
  }
  
  public static void doc2ad(Document inFile, String outFile){
	try{  	
		FileWriter fw = new FileWriter(new File(outFile));

		Element root = inFile.getRootElement();
		String str=new String();
		String strsm=new String();
		Iterator it_package = (root.getDescendants(new ElementFilter("packagedElement")));
		while (it_package.hasNext()){
			Element theNetNode = (Element)it_package.next();	

			Namespace ns=Namespace.getNamespace("xmi","http://schema.omg.org/spec/XMI/2.1");
			String ptype=theNetNode.getAttributeValue("type",ns);
			String pid=theNetNode.getAttributeValue("id",ns);
			String pname=getNameWithoutNewlines(theNetNode);
			
			//PVG: hack for also supporting statemachines embedded in a classifier (default click behavior in MD 16)
			if ("uml:Class".equals(ptype)){
				strsm+=("new :UmlClass");
				strsm+=("($=\""+pid+"\"");
				strsm+=(",name=\""+pname+"\"");
				strsm+=(")\n");
				Iterator it_cls = (theNetNode.getDescendants(new ElementFilter("ownedBehavior")));
				if (it_cls.hasNext()){
					theNetNode = (Element)it_cls.next();
					ptype=theNetNode.getAttributeValue("type",ns);
					pid=theNetNode.getAttributeValue("id",ns);
					pname=getNameWithoutNewlines(theNetNode);
				}
			}// end PVG
			
			if ("uml:StateMachine".equals(ptype)){
				strsm+=("new :StateMachine");
				strsm+=("($=\""+pid+"\"");
				strsm+=(",name=\""+pname+"\"");
				strsm+=(",visibility=Visibility::public");
				strsm+=(")\n");
				Iterator<Element> it=theNetNode.getDescendants(new ElementFilter("region"));				
				while (it.hasNext()){
					Element el1 = it.next();
					String id = el1.getAttributeValue("id",ns);
					String vis = el1.getAttributeValue("visibility");
					String name = getNameWithoutNewlines(el1);
					if (name==null) name="";
					strsm+=("new :Region");
					strsm+=("($=\""+id+"\"");
					strsm+=(",name=\""+name+"\"");
					strsm+=(",visibility=Visibility::"+vis);
					strsm+=(")\n");
					strsm+=("new @(\""+pid+"\")"+  " - :ownedElement -> @(\"" + id+ "\")\n" );

					Iterator<Element> it_region=el1.getDescendants(new ElementFilter("subvertex"));
					while (it_region.hasNext()){
						Element el_vertex = it_region.next();
						String vid = el_vertex.getAttributeValue("id",ns);
						String vvis = el_vertex.getAttributeValue("visibility");
						String vname = getNameWithoutNewlines(el_vertex);
						if (name==null) name="";
						strsm+=("new :State");
						strsm+=("($=\""+vid+"\"");
						strsm+=(",name=\""+vname+"\"");
						strsm+=(",visibility=Visibility::"+vvis);
						strsm+=(")\n");
						strsm+=("new @(\""+id+"\")"+  " - :ownedElement -> @(\"" + vid+ "\")\n" );
					}		
					// new @("5") - :ownedElement -> @("6")
								

				}

			}

			
			if ("uml:Activity".equals(ptype)){
				str+=("new :Activity");
				str+=("($=\""+pid+"\"");
				str+=(",name=\""+pname+"\"");
				str+=(",visibility=Visibility::public");
				str+=(")\n");

				Iterator<Element> it=theNetNode.getDescendants(new ElementFilter("node"));
				while (it.hasNext()){
					Element el = it.next();
					System.out.println(el.getAttributeValue("type",ns));
					if ("uml:InitialNode".equals(el.getAttributeValue("type", ns))){
						str+= mapNode(el, "InitialNode", pid, ns);
					}
					if ("uml:CallBehaviorAction".equals(el.getAttributeValue("type", ns))){
						str+= mapNode(el, "CallBehaviorAction", pid, ns);
					}
					if ("uml:CentralBufferNode".equals(el.getAttributeValue("type", ns))){
						str+= mapNode(el, "CentralBufferNode", pid, ns);
						//						if (name==null) name="this";// update PVG
						String id = el.getAttributeValue("id",ns);
						String inState = el.getAttributeValue("inState"); 						
						if (inState==null){
							Element child=el.getChild("inState");
							inState=child.getAttributeValue("idref",ns);
						}
						str+=("new @");
						str+=("(\""+id+"\")");
						str+=(" - :inState -> @");
						str+=("(\""+inState+"\"");
						str+=(")\n");
						
						String smType = el.getAttributeValue("type");
						if (smType==null){
							Element child=el.getChild("type");
							smType=child.getAttributeValue("idref",ns);
						}
						str+=("new @");
						str+=("(\""+id+"\")");
						str+=(" - :smType -> @");
						str+=("(\""+smType+"\"");
						str+=(")\n");												
					}
					if ("uml:ForkNode".equals(el.getAttributeValue("type", ns))){
						str+= mapNode(el, "ForkNode", pid, ns);
					}
					if ("uml:DecisionNode".equals(el.getAttributeValue("type", ns))){
						str+= mapNode(el, "DecisionNode", pid, ns);
					}
					if ("uml:ActivityFinalNode".equals(el.getAttributeValue("type", ns))){						
						str+= mapNode(el, "ActivityFinalNode", pid, ns);
					}
				}

				Iterator<Element> it2=theNetNode.getDescendants(new ElementFilter("edge"));
				while (it2.hasNext()){
					Element el = it2.next();
					System.out.println(el.getAttributeValue("type",ns));
					if ("uml:ControlFlow".equals(el.getAttributeValue("type", ns))||"uml:ObjectFlow".equals(el.getAttributeValue("type", ns))){
						String edgeID = el.getAttributeValue("id",ns);
						String vis = el.getAttributeValue("visibility");
						String name = el.getAttributeValue("name");
						if (name==null) name="";
						String source = el.getAttributeValue("source");
						String target = el.getAttributeValue("target");
						//new @("01") - :ownedElement -> @("02")

						System.out.println("Bingo!");
						String ctype="";
						if (el.getAttributeValue("type", ns).equals("uml:ControlFlow")){
							ctype=new String("ControlFlow");
						}
						else {
							ctype=new String("ObjectFlow");
						}
						// lookup guard
						String bGuard= null;
						Iterator<Element> it3=el.getDescendants(new ElementFilter("guard"));
						while (it3.hasNext()){
							Element guard = it3.next();
							bGuard= guard.getAttributeValue("body"); // note: the body attribute is not in the XMI namespace
							System.out.println("id: "+guard.getAttributeValue("id",ns));
							System.out.println("guard: "+bGuard);
							if (bGuard!=null && !"".equals(bGuard)){
								break;
							}
						}
						// output .grs for edge create (with or without guard init)
						if (bGuard!=null && !"".equals(bGuard)){
							str+=("new @(\""+source+"\")"+  " - "+edgeID+":"+ctype+"(guard='"+bGuard+"') -> @(\"" + target+ "\")\n" );
						} else {
							str+=("new @(\""+source+"\")"+  " - "+edgeID+":"+ctype+" -> @(\"" + target+ "\")\n" );
						}						
					}
				}
			}

			/* PVG NEW END */ 	
			
		}
		Iterator it_stereo = (root.getDescendants(new ElementFilter("automatic")));
		while (it_stereo.hasNext()){
			Element stereoNode = (Element)it_stereo.next();	

			Namespace ns=Namespace.getNamespace("xmi","http://schema.omg.org/spec/XMI/2.1");			
			String pid=stereoNode.getAttributeValue("id",ns);		
			
			str+=("new :Stereotype");
			str+=("($=\""+pid+"\"");
			str+=(",name=\"automatic\"");
			str+=(",visibility=Visibility::public");
			str+=(")\n");
			
			String baseElementRefID=stereoNode.getAttributeValue("base_CallAction");
			str+=("new @(\""+baseElementRefID+"\")"+  " - :stereotype-> @(\"" + pid+ "\")\n" );
					
		}
//		strsm+=("\n\n\n");
		
		fw.write(strsm+"\n\n\n"+str);
//		fw.write("----------------------------------------------");
		fw.flush();
//		fw.write("EERik"+str);
		
		fw.close();

  	
	} catch (Exception e) {
		e.printStackTrace();

	}
  	
  }
  
  public static void main(String[] args) {
	  if (args.length != 2) {
		System.err.println("Usage: "+args[0]+" <input> <output>");
		System.err.println("You have supplied "+args.length+" arguments.");
		return;
	  }
	  try {
		doc2ad(getDocument(args[0]),args[1]);	  
	  } catch (Exception e) { 
	      System.err.println(args[0] + " is not a well-formed UML file. AD2GRGEN has been designed for MagicDraw UML 16.6 (17.04 tested too)");
	      System.err.println(e.getMessage());
	    }  
  }
  
}
 
 
  
  


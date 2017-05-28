package umiacs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import umiacs.datastructure.ArgumentInformation;
import umiacs.datastructure.ExtractedSentence;
import umiacs.datastructure.RelationInformation;
import umiacs.tools.FileProcess;

public class EvaluateCORE {
	String filterType="ahtn";
	public boolean shouldOutput=true;
	public int nThresholdSem=3;
	private Hashtable<String,Double> hashPatternScore=new Hashtable<String,Double>();
	public EvaluateCORE()
	{
		
	}
	/**
	 * 从XML文件中读取关系
	 * @param sDirFileName
	 * @param fileExt
	 * @param vecSentence
	 */
	public void readExtractedSentence(String sInputFileName,Vector<ExtractedSentence> vecSentence,int nMaxSentenceCount)
	{
		System.out.println("readExtractedSentence");	      
//	      Vector<String> vecFileName=new Vector<String>();
//	      FileProcess.getFileList(sDirFileName, vecFileName, fileExt);
//	      int nLineNumber=0;
//	      boolean shouldBreak=false;
//	      for(int nIndex=0;nIndex<vecFileName.size();nIndex++)
	      {
//	    	  if(shouldBreak)
//	    		  break;
//	    	  String sInputFileName=vecFileName.elementAt(nIndex);
	    	  System.out.println("处理到文件 "+sInputFileName);
	    	  try { 
	          		SAXBuilder builder = new SAXBuilder(); 
	          		Document doc = builder.build(new File(sInputFileName)); 
	          		Element books=doc.getRootElement();
	          		List sentencelist=books.getChildren("sentence");
	          		System.out.println("sentencelist size = "+sentencelist.size());
	          		int current_sentence_id=0;
	          		int sentence_file_id=0;
	          		boolean shouldOutput=false;
	          		
	          		for (Iterator iter = sentencelist.iterator(); iter.hasNext();) {
	          				          				
	          			Element sentence = (Element) iter.next();
	          			if(current_sentence_id>=nMaxSentenceCount)
	          				break;
	          			current_sentence_id++;
	          			List origin_test_list=sentence.getChildren("origin_text");
	          			ExtractedSentence sentenceInfo=new ExtractedSentence();
	          			if(origin_test_list.size()>0)
	          			{
	          				Element origin_text = (Element) origin_test_list.get(0);
	          				sentenceInfo.origin_text=(String)origin_text.getText();
	          			}
	          			sentenceInfo.id=Integer.parseInt((String)sentence.getAttributeValue("id"));
	          			List relationlist=sentence.getChildren("relation");
	          			for(Iterator iterS = relationlist.iterator(); iterS.hasNext();)
	          			{
	          				boolean isContainAddedSubject=false;
	          				//relation
	          				Element relation = (Element) iterS.next();
	          				List argumentlist=relation.getChildren("argument");	    
	          				RelationInformation r=new RelationInformation();
	          				if(relation.getAttributeValue("pattern")!=null)
	          					r.semantic_pattern=(String)relation.getAttributeValue("pattern").replace("_", " ");
	          				if(relation.getAttributeValue("syntax_pattern")!=null)
	          					r.syntax_pattern=(String)relation.getAttributeValue("syntax_pattern").replace("_", " ");
	          				if(relation.getAttributeValue("pred")!=null)
	          					r.predicate=(String)relation.getAttributeValue("pred");
	          				if(relation.getAttributeValue("feature")!=null)
	          					r.feature=(String)relation.getAttributeValue("feature");
	          				if(relation.getAttributeValue("isDeStructure")!=null)
	          					r.isDeStructure=Boolean.parseBoolean((String)relation.getAttributeValue("isDeStructure"));
	          				if(relation.getAttributeValue("taggedType")!=null)
	          				{
	          					r.syntax_pattern=((String)relation.getAttributeValue("taggedType"));
	          					if(this.filterType.indexOf(r.syntax_pattern)<0 && sInputFileName.indexOf("human")<0)
	          						continue;
	          				}
		          			for(Iterator iterA = argumentlist.iterator(); iterA.hasNext();)
		          			{
		          				//argument
		          				Element argument = (Element) iterA.next();
		          				ArgumentInformation arg=new ArgumentInformation();
		          				if(argument.getAttributeValue("node_id")!=null)
		          				{
		          					String temp=(String)argument.getAttributeValue("node_id");
		          					if(temp.trim().length()>0)
		          						arg.nNodeID=Integer.parseInt(temp);
		          				}
		          				if(argument.getAttributeValue("head_word")!=null)
		          				{
		          					arg.head_word=(String)argument.getAttributeValue("head_word");
		          					arg.head_word=FileProcess.QBTransfer(arg.head_word);
		          				}
		          				if(argument.getAttributeValue("content")!=null)
		          				{
		          					arg.content=(String)argument.getAttributeValue("content");
		          					arg.content=FileProcess.QBTransfer(arg.content);
		          					if(arg.content.endsWith("(Z)"))
		          					{
		          						isContainAddedSubject=true;
		          						arg.content=arg.content.substring(0,arg.content.indexOf("(Z)"));
		          					}
		          				}
		          				if(argument.getAttributeValue("sem_cat")!=null)
		          					arg.sem_cat=(String)argument.getAttributeValue("sem_cat");
		          				
								r.arguArr.add(arg);	
		          			}		          			
		          			if(!isContainAddedSubject)
		          				sentenceInfo.vecRelation.add(r);	
		          				          				
	          			}	  
	          			vecSentence.add(sentenceInfo);
	          			
	          		}
	          		
	          	}catch (Exception e) { 
	          		e.printStackTrace(); 
	          	} 
	      }
	    		  
	}
	public void compareTwo(Vector<ExtractedSentence> vecHumanSentence,Vector<ExtractedSentence> vecAutoSentence)
	{
		int nAutoRelationCount=0;
		int nCorrectRelationCount=0;
		int nTaggedRelationCount=0;
		
		int nSemTaggedRelationCount=0;
		
		int nAutoArgumentCount=0;
		int nCorrectArgumentCount=0;
		int nTaggedArgumentCount=0;
		
		int nAutoPredCount=0;
		int nCorrectPredCount=0;
		int nTaggedPredCount=0;
		
		for(int i=0;i<vecHumanSentence.size();i++)
		{
//			nTaggedRelationCount+=vecHumanSentence.elementAt(i).vecRelation.size();
			for(RelationInformation relationHuman:vecHumanSentence.elementAt(i).vecRelation)
			{
				if(relationHuman.predicate.equalsIgnoreCase("is") ||relationHuman.predicate.equalsIgnoreCase("de"))
					continue;
				else
					nTaggedRelationCount+=1;
			}
			nAutoRelationCount+=vecAutoSentence.elementAt(i).vecRelation.size();
			
			ExtractedSentence sentenceHuman=vecHumanSentence.elementAt(i);
			ExtractedSentence sentenceAuto=vecAutoSentence.elementAt(i);
			if(!sentenceHuman.origin_text.trim().equalsIgnoreCase(sentenceAuto.origin_text.trim()))
			{
//				System.out.println("sentenceHuman !=sentenceAuto "+i);
//				System.out.println("begin"+sentenceHuman.origin_text);
//				System.out.println(sentenceAuto.origin_text);
			}
			
			Hashtable<Integer,Boolean> hashMatched=new Hashtable<Integer,Boolean>();
			for(int k=0;k<sentenceAuto.vecRelation.size();k++)
			{
				RelationInformation relationAuto=sentenceAuto.vecRelation.elementAt(k);
				relationAuto.hasUntagged=false;
			}
			for(int j=0;j<sentenceAuto.vecRelation.size();j++)
			{
				RelationInformation relationAuto=sentenceAuto.vecRelation.elementAt(j);
				int nMatchedCount=0;
				int nMaxMatchedScore=0;
				int nMatchedNumber=-1;
				boolean isAllTagged=false;
				
				for(int k=0;k<sentenceHuman.vecRelation.size();k++)
				{
					if(hashMatched.get(k)!=null)
						continue;
					RelationInformation relationHuman=sentenceHuman.vecRelation.elementAt(k);
					//First, try to match the predicate
					if(relationHuman.predicate.equalsIgnoreCase(relationAuto.predicate))
					{
						nMatchedCount++;
						int nMatchedScore=0;
						for(int l=0;l<relationHuman.arguArr.size();l++)
						{
							ArgumentInformation argumentHuman=relationHuman.arguArr.get(l);
							
							for(int m=0;m<relationAuto.arguArr.size();m++)
							{
								ArgumentInformation argumentAuto=relationAuto.arguArr.get(m);
								if(argumentHuman.content.trim().equals(argumentAuto.content.trim()))
								{
									nMatchedScore++;
									break;
								}
							}
							
						}
						if(nMaxMatchedScore<nMatchedScore)
						{
							nMaxMatchedScore=nMatchedScore;
							nMatchedNumber=k;
							if(nMaxMatchedScore==relationHuman.arguArr.size() && nMaxMatchedScore==relationAuto.arguArr.size())
							{
								isAllTagged=true;
							}
							else 
								isAllTagged=false;
						}
					}					
					
					
				}
				if(nMaxMatchedScore>0)
				{
					hashMatched.put(nMatchedNumber, true);
					
					if(isAllTagged)
					{
						nCorrectRelationCount++;
						relationAuto.hasUntagged=true;
						String pattern=relationAuto.semantic_pattern.replaceAll(" ", "_");
						Double score=this.hashPatternScore.get(pattern);
					}
					nCorrectPredCount++;					
					nAutoArgumentCount+=relationAuto.arguArr.size();
					nCorrectArgumentCount+=nMaxMatchedScore;
				}
			}
			for(int k=0;k<sentenceHuman.vecRelation.size();k++)
			{
				RelationInformation relationHuman=sentenceHuman.vecRelation.elementAt(k);
				if(relationHuman.predicate.equalsIgnoreCase("is") ||relationHuman.predicate.equalsIgnoreCase("de"))
					continue;
				nTaggedArgumentCount+=relationHuman.arguArr.size();
			}
			
		}
		int nCorrect=10;
		int nFalse=3;
		for(int i=0;i<vecAutoSentence.size();i++)
		{
			ExtractedSentence sen=vecAutoSentence.elementAt(i);
			for(int j=0;j<sen.vecRelation.size();j++)
			{
				RelationInformation r=sen.vecRelation.get(j);
				String pattern=r.semantic_pattern.replaceAll(" ", "_");
				Double score=this.hashPatternScore.get(pattern);
//				System.out.println(pattern);
			}
		}
		for(int i=0;i<vecAutoSentence.size();i++)
		{
			ExtractedSentence sen=vecAutoSentence.elementAt(i);
			for(int j=0;j<sen.vecRelation.size();j++)
			{
				RelationInformation r=sen.vecRelation.get(j);
				String pattern=r.semantic_pattern.replaceAll(" ", "_");
				Double score=this.hashPatternScore.get(pattern);
				if(!r.hasUntagged && score!=null && filterType.indexOf(r.syntax_pattern.trim())>=0)
				{
//					System.out.println("false "+score+" "+pattern+" "+sen.origin_text);
					nFalse++;
				}
			}
			
		}
		System.out.println("nCorrect:"+nCorrect+" nFalse: "+nFalse);
		if(shouldOutput)
		{
			float nWeightF=1f;
			float p=((float)nCorrect/(nCorrect+nFalse));
			float r=((float)nCorrect/nTaggedRelationCount);
			float f=((nWeightF*nWeightF+1)*p*r)/(nWeightF*nWeightF*p+r);
			System.out.println("threshold="+this.nThresholdSem);
			System.out.println("关系标注正确率为：\tp= "+p);
			System.out.println("关系标注召回率为： \tr="+r);
			System.out.println("F为： \tF1="+f);
		}
		if(shouldOutput)
		{
			System.out.println("总计人工标注的关系数为： "+nTaggedRelationCount+" 自动标注关系数："+nAutoRelationCount+" 正确标注数："+nCorrectRelationCount);
			float nWeightF=1f;
			float p=((float)nCorrectRelationCount/nAutoRelationCount);
			float r=((float)nCorrectRelationCount/nTaggedRelationCount);
			float f=((nWeightF*nWeightF+1)*p*r)/(nWeightF*nWeightF*p+r);
			System.out.println("关系标注正确率为：\tp= "+p);
			System.out.println("关系标注召回率为： \tr="+r);
			System.out.println("F为： \tF1="+f);
			
			System.out.println("谓词标注正确数为： \t"+nCorrectPredCount);
			p=((float)nCorrectPredCount/nAutoRelationCount);
			r=((float)nCorrectPredCount/nTaggedRelationCount);
			f=((nWeightF*nWeightF+1)*p*r)/(nWeightF*nWeightF*p+r);
			System.out.println("谓词标注正确率为：\tp= "+p);
			System.out.println("谓词标注召回率为： \tr="+r);
			System.out.println("谓词标注F为： \tF1="+f);
			
			p=((float)nCorrectArgumentCount/nAutoArgumentCount);
			r=((float)nCorrectArgumentCount/nTaggedArgumentCount);
			f=((nWeightF*nWeightF+1)*p*r)/(nWeightF*nWeightF*p+r);
			System.out.println("论元标注正确率为：\tp= "+p);
			System.out.println("论元标注召回率为： \tr="+r);
			System.out.println("论元标注F为： \tF1="+f);
		}
		
		
	}
	public Vector<ExtractedSentence> evaluate(String sHumanTaggedFileName,String sAutoTaggedFileName,String sOutputFileName)
	{
		if(shouldOutput)
		System.out.println("evaluate sHumanTaggedFileName="+sHumanTaggedFileName+" sAutoTaggedFileName"+
				sAutoTaggedFileName+" filterType="+this.filterType);
		
		Vector<ExtractedSentence> vecHumanSentence=new Vector<ExtractedSentence>();
		Vector<ExtractedSentence> vecAutoSentence=new Vector<ExtractedSentence>();
		int nSentenceCount=500;
		this.readExtractedSentence(sHumanTaggedFileName, vecHumanSentence,nSentenceCount);
		this.readExtractedSentence(sAutoTaggedFileName, vecAutoSentence,nSentenceCount);	
		if(shouldOutput)
		System.out.println("vecHumanSentence.size="+vecHumanSentence.size()+" vecAutoSentence.size="+vecAutoSentence.size());
		if(vecHumanSentence.size()==vecAutoSentence.size())
		{
			compareTwo(vecHumanSentence,vecAutoSentence);
		}
		else
		{
			if(shouldOutput)
			System.out.println("vecHumanSentence.size()!=vecAutoSentence.size()");
		}
		ExtractRelationNew extractor=new ExtractRelationNew();
		extractor.outputToFileForMannuaAnnotation(sOutputFileName, vecAutoSentence);
		return vecAutoSentence;
	}
	public void evaluate(String sHumanTaggedFileName,String sAutoTaggedFileName,String sOutputFileName,Vector<ExtractedSentence> vecHumanSentence,Vector<ExtractedSentence> vecAutoSentence)
	{
		if(shouldOutput)
		System.out.println("evaluate sHumanTaggedFileName="+sHumanTaggedFileName+" sAutoTaggedFileName"+
				sAutoTaggedFileName+" filterType="+this.filterType);		
		int nSentenceCount=500;
		this.readExtractedSentence(sHumanTaggedFileName, vecHumanSentence,nSentenceCount);
		this.readExtractedSentence(sAutoTaggedFileName, vecAutoSentence,nSentenceCount);	
		if(shouldOutput)
		System.out.println("vecHumanSentence.size="+vecHumanSentence.size()+" vecAutoSentence.size="+vecAutoSentence.size());
		if(vecHumanSentence.size()==vecAutoSentence.size())
		{
			compareTwo(vecHumanSentence,vecAutoSentence);
		}
		else
		{
			if(shouldOutput)
			System.out.println("vecHumanSentence.size()!=vecAutoSentence.size()");
		}
		ExtractRelationNew extractor=new ExtractRelationNew();
		extractor.outputToFileForMannuaAnnotation(sOutputFileName, vecAutoSentence);
		return;
	}
	public void readPatternScore(String sInputFileName,String sCharset)
	{
		BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new
                    FileInputStream(sInputFileName),sCharset));
            
            String sLine="";
            int nLineNumber=0;
            System.out.println("正常处理");
            while ((sLine = reader.readLine()) != null) 
            {    
          	  nLineNumber++;
          	  if(nLineNumber%10000==0)
          	  System.out.println("Is reading "+nLineNumber);
          	  sLine=sLine.trim();
          	  if(true)
          	  {
          		  String[]token_list=sLine.split("\t");
          		  if(token_list.length>=4)
          		  {
          			  int nFreq=Integer.parseInt(token_list[1]);
//          			  if(nFreq>=this.nThresholdSem)
//          				  this.hashPatternScore.put(token_list[1].trim(), Double.parseDouble(token_list[0]));
          			if(nFreq>=this.nThresholdSem)
        				  this.hashPatternScore.put(token_list[1].trim(), (double)nFreq);
          		  }
          	  }
            }
        }catch(IOException e)
        {
        	e.printStackTrace();
        }
        System.out.println("hashPatternScore size = "+this.hashPatternScore.size());
	}
	public static void main(String[]args)
	{
			EvaluateCORE evaluate=new EvaluateCORE();
			String sHumanTaggedFileName="tagged/relation-500-human.xml";
			String sAutoTaggedFileName="result_origin_rule/relation-500.txt";
			String sOutputFileName="score.txt";
			evaluate.nThresholdSem=3;
			evaluate.filterType="tn";
			System.out.println("evaluate.filterType="+evaluate.filterType);
			String sPatterFileName="result_origin_rule/pattern_synset.txt";
			evaluate.readPatternScore(sPatterFileName,"utf-8");
			evaluate.evaluate(sHumanTaggedFileName, sAutoTaggedFileName,sOutputFileName);
		
	}

}

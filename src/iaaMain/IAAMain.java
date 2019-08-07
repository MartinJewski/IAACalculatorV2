package iaaMain;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.PercentageAgreement;
import org.dkpro.statistics.agreement.coding.RandolphKappaAgreement;
import org.dkpro.statistics.agreement.coding.WeightedAgreement;

import Annotators.AnnotatorManager;
import Display.Show;
import Factory.IAAFactory;
import iaaExport.IAAExporter;
import iaaGenerator.IAACalculator;
import iaaGenerator.IAAExtractor;

public class IAAMain {

	public static void main(String[] args) throws IOException {
		
		System.out.println("-----------------important message------------------");
		System.out.println("Do not run this jar twice without");
		System.out.println("saving your first result, because the second");
		System.out.println("run will be appended to your first output files");
		System.out.println("-----------------------------------------------------");
		System.out.println(" ");
		System.out.println(" ");
		System.out.println("Please insert your path to your iaa tsv.");
		System.out.println("Important: the tsv file must have the following structure");
		System.out.println("satzID, verb, satz, annotator1, ......, AnotatorN, null");
		System.out.print("in: ");
		
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String inPath = reader.readLine();
        //"dbg = debug mode, skips all the input and uses predefined paths for testing
        if(!inPath.equals("dbg")) {

            System.out.println("given input path: " + inPath);   
            
            System.out.println("please insert your output path");
    		System.out.print("out: ");
            String out = reader.readLine();
            
            System.out.println("please insert for your desired calculation method.");
            System.out.println("Percentage, RandolphKappa, Bennetts");
     		
            System.out.print("Method: "); 
            String iaaMethod = reader.readLine();
        
            
            start(inPath, out, iaaMethod);
        }else {
        	
        	start("./TSVFiles/iaa_old.tsv", "C:/Users/Marrin/Desktop/a", 
        			IAACalculator.IAAMethods.Percentage.toString());
        }
		
	}
	
	
	public static void start(String inPath, String outPath, String iaaMathod) throws IOException {

		IAAExtractor ex = new IAAExtractor(inPath, 0);
		
		AnnotatorManager annoManager = new AnnotatorManager();
		annoManager.addAnnotators(ex.extractAnnotators());

		IAACalculator iaaCalc = new IAACalculator(ex, annoManager);
		
		IAAFactory factory = 
				new IAAFactory(iaaCalc, ex, annoManager, iaaCalc.parseIAAMethod(iaaMathod)); 
		
		String out = outPath;
		File folder_out = new File(out, "IAA");
		folder_out.mkdir();
		
		File out_iaa_verbs = new File(folder_out, "IAA_Verbs");
		out_iaa_verbs.mkdir();
		File out_sensesList_verbs = new File(folder_out, "SensesLists_Verbs");
		out_sensesList_verbs.mkdir();
		
		File out_sensesIAA_verbs = new File(folder_out, "SensesLists_IAA");
		out_sensesIAA_verbs.mkdir();
		
		IAAExporter.exportListSA(factory.getOverallIaa(),
				folder_out.getAbsolutePath(), "OverallIAA.txt");
		IAAExporter.exportListSA(factory.getOverallIaaPerVerb(),
				folder_out.getAbsolutePath(), "OverallIAAPerVerb.txt");
		IAAExporter.exportNestedListSA(factory.getpairwiseOverAllRows(), 
				folder_out.getAbsolutePath(), "PairwiseOverallRows.txt");
		IAAExporter.exportListS(annoManager.getAnnotatorsNames(), 
				folder_out.getAbsolutePath(), "All_Annotators.txt");
		IAAExporter.exportListSA(factory.getOverallIaaSensesPerVerb(),
				folder_out.getAbsolutePath(), "OverallIAASensesPerVerb.txt");
		
		
		for(String key : ex.getAllVerbs()) {
			IAAExporter.exportNestedListSA(factory.getPairwiseVerbIaa().get(key),
					out_iaa_verbs.getAbsolutePath(), "PairwiseForVerb_" + key + ".txt");
		}
		
		for(String key : ex.getAllSenses()) {
			IAAExporter.exportNestedListSA(factory.getPairwiseSensesIaa().get(key),
					out_sensesIAA_verbs.getAbsolutePath(), "PairwiseForSense_" + key + ".txt");
		}

		IAAExporter.exportDouble("corr_between_number_senses_per_verb_and_num_sentences_per_verb", 
				iaaCalc.calculateCorr(iaaCalc.getNumSensesPV(), 
				iaaCalc.getNumSentencesPV()),
				out, "CORR_SensesPV_SentencesPV.txt");
		
		IAAExporter.exportDouble("corr_between_iaa_per_verb_and_senses_per_verb",
				iaaCalc.calculateCorr(factory.getOverallIaaPerVerb(), 
				iaaCalc.getNumSensesPV()),
			    out, "CORR_IAA_PerVerb_and_SensesPV.txt");
		
		
		IAAExporter.exportListS(ex.getAllVerbs(), out_iaa_verbs.getAbsolutePath()
				, "All_used_verbs.txt");
		
		IAAExporter.exportListS(ex.getAllSenses(), out_sensesList_verbs.getAbsolutePath(), "All_used_senses.txt");
		
		
		for(String k: ex.getAllVerbs()) {
			
			List<String> tmp = ex.getSensesPerVerb().get(k);
			tmp.add(0, k);
			
			IAAExporter.exportListS(tmp, out_sensesList_verbs.getAbsolutePath()
					, "SensesList_" + k + ".txt");
		}
	}
}

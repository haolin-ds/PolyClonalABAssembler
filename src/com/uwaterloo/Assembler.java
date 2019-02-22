package com.uwaterloo;

import com.uwaterloo.Reader.PSMIonsReader;
import com.uwaterloo.Reader.PSMReader;
import com.uwaterloo.Reader.ProteinPeptideReader;
import com.uwaterloo.Reader.TemplatesLoader;

import java.util.*;

public class Assembler {
    public Assembler() {

    }

    public void process() {
        String dir = "D:\\Hao\\data\\for_analysis\\polyclonalAssemblerData\\";
        dir = "D:\\Hao\\data\\for_analysis\\PolyClonal_ab19001_SPIDER_12\\";
        //dir = "D:\\Hao\\result\\Waters_mAB_SPIDER_13\\";
        String psmFile = dir + "DB search psm.csv";
        PSMReader psmReader = new PSMReader();
        List<PSM> psmList = psmReader.readCSVFile(psmFile);

        String psmIonsFile = dir + "PSM ions.csv";
        PSMIonsReader ionsReader = new PSMIonsReader();
        HashMap<String, short[]> scanIonPosesMap = ionsReader.readPSMIonsFile(psmIonsFile);
        HashMap<String, short[]> scanIonScoresMap = ionsReader.setIonsScore(scanIonPosesMap);
        setIonScoresForPSMList(psmList, scanIonScoresMap);


        String templateFasta = dir + "Nuno.2016.heavy.template.fasta";
        templateFasta = dir + "ab19001.template_top8.fasta";
        //templateFasta = dir + "Waters_mAB.template_top4.fasta";
        TemplatesLoader loader = new TemplatesLoader();
        List<Template> templateList = loader.loadTemplateFasta(templateFasta);

        ProteinPeptideReader ppReader = new ProteinPeptideReader(templateList);
        String proteinPeptideFile = dir + "protein-peptides.csv";
        HashMap<String, List<TMapPosition>> peptideProteinMap = ppReader.readProteinPeptideFile(proteinPeptideFile);


        TemplatePSMsAligner aligner = new TemplatePSMsAligner();
        List<TemplateHooked> templateHookedList = aligner.alignPSMstoTemplate(psmList, templateList, peptideProteinMap);
        ArrayList<ArrayList<PSMAligned>> listOfPSMAlignedList = aligner.getPsmAlignedList();


        for (int templateId = 0; templateId < templateHookedList.size(); templateId++) {
            System.out.println("Template " + templateId);
            TemplateHooked aTemplateHooked = templateHookedList.get(templateId);
            List<char[]> top2CandidateTemplates = findCandidateForOneTemplate(aTemplateHooked, templateId, listOfPSMAlignedList);
            templateHookedList.get(templateId).setModifiedTemplates(top2CandidateTemplates);
            //Debug
            break;
        }


        for (int templateId = 0; templateId < templateHookedList.size(); templateId++) {
            List<char[]> candidateTemplates = templateHookedList.get(templateId).getModifiedSeq();
            String templateAccession = templateHookedList.get(templateId).getTemplateAccession();
            for (int i = 0; i < candidateTemplates.size(); i++) {
                System.out.println(">can" + (i + 1) + "_" + templateAccession);
                System.out.println(new String(candidateTemplates.get(i)));
            }
            //Debug
            break;
        }

    }

    /**
     * For each psm in psmList, try to fill in the fragment ion scores.
     * @param psmList
     * @param scanIonScoresMap
     */
    private void setIonScoresForPSMList(List<PSM> psmList, HashMap<String,short[]> scanIonScoresMap) {
        for (int i = 0; i < psmList.size(); i++) {
            String scan = psmList.get(i).getScan();
            if (scanIonScoresMap.containsKey(scan)) {
                psmList.get(i).setIonScores(scanIonScoresMap.get(scan));
            } else {
                System.err.println("scan " + scan + " does not contain fragment ions information!");
            }
        }
    }

    private List<char[]> findCandidateForOneTemplate(TemplateHooked aTemplateHooked, int templateId,
                                             ArrayList<ArrayList<PSMAligned>> listOfPSMAlignedList) {

        MapScanPSMAligned scanPSMMapper = new MapScanPSMAligned(listOfPSMAlignedList.get(templateId));
        HashMap<String, PSMAligned> scanPSMMap = scanPSMMapper.getScanPSMMap();

        MutationValidator validator = new MutationValidator();
        List<HashMap<List<Integer>, List<MutationsPattern>>> mutationsOnTemplateList = validator.findSignificantMutations(aTemplateHooked, scanPSMMap);
        //  printMutationsOnTemplate(mutationsOnTemplateList);

        TemplateCandidateBuilder templateCandidateBuilder = new TemplateCandidateBuilder(mutationsOnTemplateList);
        List<char[]> top2CandidateTemplates = templateCandidateBuilder.buildCandidateTemplate(aTemplateHooked, scanPSMMap);

        return top2CandidateTemplates;

    }

    private void printMutationsOnTemplate(List<HashMap<List<Integer>, List<String>>> mutationsOnTemplateList) {
        for (int i = 0; i < mutationsOnTemplateList.size(); i++) {
            HashMap<List<Integer>, List<String>> mutationsOnTemplate = mutationsOnTemplateList.get(i);
            for (Map.Entry entry : mutationsOnTemplate.entrySet()) {
                List<Integer> posList = (List<Integer>) entry.getKey();
                List<String> patternList = (List<String>) entry.getValue();
                for (int pos : posList) {
                    System.out.print(pos + " ");
                }
                System.out.println();
                for (String pattern : patternList) {
                    System.out.println(pattern);
                }

            }
        }

    }


    private void printMutationsSortedByPos(TreeMap<Integer, List<String>> mutationsSortedByPos) {
        for (Map.Entry entry : mutationsSortedByPos.entrySet()) {
            System.out.println(entry.getKey());
            List<String> patternList = (List<String>) entry.getValue();

            for (String pattern : patternList) {
                System.out.println(pattern);
            }

        }
    }
    public void testMutationValidator(TemplateHooked templateHooked) {
        MutationValidator validator = new MutationValidator();
        int maxMutationNum = validator.findMaxMutationNumPerPSM(templateHooked);
        System.out.println("Max mutation num: " + maxMutationNum);
        int mutationNum = 3;
        List<Integer> posWithMaxMutationNumList = validator.extractPositionWithMutationNum(templateHooked, mutationNum);

        for (Integer pos : posWithMaxMutationNumList) {
            List<PSMAligned> psmAlignedList = templateHooked.getSpiderList().get(pos);
            System.out.print("pos: " + pos);
            for (PSMAligned psmAligned : psmAlignedList) {
                if (psmAligned.getPositionOfVariations().size() == mutationNum) {
                    System.out.print(" " + psmAligned.getScan());
                }
            }
            System.out.println();

        }



    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.oncokb.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.mskcc.cbio.oncokb.bo.GeneBo;
import org.mskcc.cbio.oncokb.model.Alteration;
import org.mskcc.cbio.oncokb.model.AlterationType;
import org.mskcc.cbio.oncokb.model.Gene;
import org.mskcc.cbio.oncokb.util.AlterationUtils;
import org.mskcc.cbio.oncokb.util.ApplicationContextSingleton;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author jgao
 */
@Controller
public class VariantAnnotationXMLV2Controller {
    @RequestMapping(value="/var_annotation_v2", method=RequestMethod.POST,
            produces="application/xml;charset=UTF-8")
    public @ResponseBody String getVariantAnnotation(
            @RequestParam(value="file", required=true) MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                List<Alteration> alterations = new ArrayList<Alteration>();
                List<String> alterationXmls = new ArrayList<String>();
                
                InputStream is = file.getInputStream();
                SAXReader reader = new SAXReader();
                Document document = reader.read(is);
                
                // sample id
                String sampleId = document.selectSingleNode("//xml/sample_id").getText();
                
                // test id
                String testId = document.selectSingleNode("//xml/test_id").getText();
                
                // diagnosis
                String diagnosis = document.selectSingleNode("//xml/diagnosis").getText();
                
                // Somatic variants
                runVcf2Maf(document.asXML(),alterations, alterationXmls, diagnosis);
//                List<Node> vcfNodes = document.selectNodes("//xml/somatic_variants/data");
//                for (Node vcfNode : vcfNodes) {
//                    handleSomaticVariants(vcfNode, alterations, alterationXmls, diagnosis);
//                }
                
                // Copy number alterations
                List<Node> cnaNodes = document.selectNodes("//xml/copy_number_alterations/copy_number_alteration");
                for (Node cnaNode : cnaNodes) {
                    handleCNA(cnaNode, alterations, alterationXmls, diagnosis);
                }
                
                // fusions
                List<Node> fusionNodes = document.selectNodes("//xml/fusion_genes/fusion_gene");
                for (Node fusionNode : fusionNodes) {
                    handleFusion(fusionNode, alterations, alterationXmls, diagnosis);
                }
                
                // exporting
                StringBuilder sb = new StringBuilder();
                
                // meta
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
                sb.append("<document>\n");
                sb.append("<date_generated>2015-01-09</date_generated>\n");
                sb.append("<oncokb_api_version>0.2</oncokb_api_version>\n");
                sb.append("<ensembl_version>79</ensembl_version>\n");
                sb.append("<sample>");
                sb.append("<sample_id>").append(sampleId).append("</sample_id>\n");
                sb.append("<diagnosis>").append(diagnosis).append("</diagnosis>\n");
                sb.append("<test>\n");
                sb.append("<test_id>").append(testId).append("</test_id>\n");
                
                // variants
                sb.append(exportVariants(alterationXmls));
                
                // variant_interation
                
                // end
                sb.append("</test>\n");
                sb.append("</sample>\n");
                sb.append("</document>");
                
                return sb.toString();
            } catch (IOException | DocumentException ex) {
                return "<xml>Failed to upload file.</xml>";
            }
        } else {
            return "<xml>Failed to upload file.</xml>";
        }
    }
    
    private void handleSomaticVariants(Node vcfNode, List<Alteration> alterations, List<String> alterationXmls, String diagnosis) {
        // do it here
    }
    
    /**
     * This is a hacky way to run VEP. We should switch to web service once that is ready.
     */
    private void runVcf2Maf(String inputXml, List<Alteration> alterations, List<String> alterationXmls, String diagnosis) throws IOException, DocumentException {
        File tmpFile = File.createTempFile("temp-oncokb-input-", ".xml");
        String inputPath = tmpFile.getAbsolutePath();
        String outputPath = inputPath.substring(0,inputPath.length()-3) + "oncokb.xml";
        
        FileWriter writer = new FileWriter(tmpFile);
        writer.append(inputXml);
        writer.close();
        
        Process proc = Runtime.getRuntime().exec("perl $VEP_MAF_XML_PL "+inputPath);
        
        SAXReader reader = new SAXReader();
        Document document = reader.read(outputPath);
        
        List<Node> variantNodes = document.selectNodes("//document/sample/variant");
        for (Node node : variantNodes) {
            String alterationXml = "<variant_type>small_nucleotide_variant</variant_type>\n"
                    + node.selectSingleNode("genomic_locus").asXML()
                    + node.selectSingleNode("allele").asXML();
            alterationXmls.add(alterationXml);
            
            String geneSymbol = node.selectSingleNode("allele/transcript/hgnc_symbol").getText();
            GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
            Gene gene = geneBo.findGeneByHugoSymbol(geneSymbol);
            
            String proteinChange = node.selectSingleNode("allele/transcript/hgvs_p_short").getText();
            
            Alteration alteration = new Alteration();
            alteration.setAlterationType(AlterationType.MUTATION);
            alteration.setGene(gene);
            alteration.setName(proteinChange);
            
            AlterationUtils.annotateAlteration(alteration, proteinChange);
            alterations.add(alteration);
        }

    }
    
    private void handleCNA(Node cnaNode, List<Alteration> alterations, List<String> alterationXmls, String diagnosis) {
        Gene gene = parseGene(cnaNode, "gene");
        if (gene==null) {
            return;
        }
        
        String type = cnaNode.selectSingleNode("type").getText();
        
        Alteration alteration = new Alteration();
        alteration.setGene(gene);
        alteration.setAlteration(type);
        alteration.setAlterationType(AlterationType.MUTATION); // TODO: this needs to be fixed
        
        alterations.add(alteration);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<variant_type>copy_number_alteration</variant_type>\n");
        sb.append(cnaNode.asXML()).append("\n");
        sb.append(VariantAnnotationXMLController.annotate(alteration, diagnosis));
        alterationXmls.add(sb.toString());
    }
    
    private void handleFusion(Node fusionNode, List<Alteration> alterations, List<String> alterationXmls, String diagnosis) {
        Gene gene1 = parseGene(fusionNode, "gene_1");
        Gene gene2 = parseGene(fusionNode, "gene_2");
        
        if (gene1==null || gene2==null) {
            return;
        }
        
//        String type = fusionNode.selectSingleNode("type").getText();
        
        Alteration alteration = new Alteration();
        alteration.setGene(gene2);
        alteration.setAlteration(gene1.getHugoSymbol()+"-"+gene2.getHugoSymbol()+" fusion");
        alteration.setAlterationType(AlterationType.MUTATION); // TODO: this needs to be fixed
        
        alterations.add(alteration);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<variant_type>fusion_gene</variant_type>\n");
        sb.append(fusionNode.asXML()).append("\n");
        sb.append(VariantAnnotationXMLController.annotate(alteration, diagnosis));
        alterationXmls.add(sb.toString());
    }
    
    private Gene parseGene(Node node, String genePath) {
        GeneBo geneBo = ApplicationContextSingleton.getGeneBo();
        Gene gene = null;
        Node entrezGeneIdNode = node.selectSingleNode(genePath+"/entrez_gene_id");
        if (entrezGeneIdNode!=null) {
            gene = geneBo.findGeneByEntrezGeneId(Integer.parseInt(entrezGeneIdNode.getText()));
        } else {
            Node hugoSymbolNode = node.selectSingleNode(genePath+"/hgnc_symbol");
            if (hugoSymbolNode!=null) {
                gene = geneBo.findGeneByHugoSymbol(hugoSymbolNode.getText());
            }
        }
        return gene;
    }
    
    private String exportVariants(List<String> alterationXmls) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<alterationXmls.size(); i++) {
            String xml = alterationXmls.get(i);
            sb.append("<variant no=\"").append(i+1).append("\">\n").append(xml).append("</variant>\n");
        }
        return sb.toString();
    }
    
}

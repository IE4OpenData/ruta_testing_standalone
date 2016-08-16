package org.ie4opendata.ruta_testing_standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.XMLInputSource;
import org.ie4opendata.ruta_testing_standalone.testing.EvalDataProcessor;
import org.ie4opendata.ruta_testing_standalone.testing.TestCasData;
import org.ie4opendata.ruta_testing_standalone.testing.TypeEvalData;
import org.ie4opendata.ruta_testing_standalone.testing.evaluator.ICasEvaluator;
import org.ie4opendata.ruta_testing_standalone.testing.evaluator.ICasEvaluatorFactory;
import org.xml.sax.SAXException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * Usage: Evaluate <gold XMI folder> <system XMI folder> <evaluator class> <typesystem>
 * 
 */
public class Evaluate {

  private static void writeXmi(CAS aCas, File file) throws IOException, SAXException {
    FileOutputStream out = null;
    try {
      file.getParentFile().mkdirs();
      out = new FileOutputStream(file);
      XmiCasSerializer.serialize(aCas, out);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  private static void deserializeCASs(CAS tdCas, TestCasData td, CAS casA, File fileA)
          throws FileNotFoundException, SAXException, IOException {
    if (!fileA.exists()) {
      throw new FileNotFoundException(fileA.getAbsolutePath());
    }
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(td.getPath());
      XmiCasDeserializer.deserialize(inputStream, tdCas, true);
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
    try {
      inputStream = new FileInputStream(fileA);
      XmiCasDeserializer.deserialize(inputStream, casA, true);
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  private static void prepareCas(CAS cas) {
    if (!args.includedTypes.isEmpty()) {
      // exclude all other types if there are some included types
      args.excludedTypes = new ArrayList<String>();
      List<Type> types = cas.getTypeSystem().getProperlySubsumedTypes(cas.getAnnotationType());
      for (Type type : types) {
        if (!args.includedTypes.contains(type.getName())) {
          args.excludedTypes.add(type.getName());
        }
      }
    }
    if (args.includedTypes.isEmpty() && args.excludedTypes.isEmpty()) {
      // remove all annotation in default settings
      String documentText = cas.getDocumentText();
      cas.reset();
      cas.setDocumentText(documentText);
    } else {
      List<AnnotationFS> toRemove = new LinkedList<AnnotationFS>();
      AnnotationIndex<AnnotationFS> annotationIndex = cas.getAnnotationIndex();
      for (AnnotationFS annotationFS : annotationIndex) {
        Type type = annotationFS.getType();
        String typeName = type.getName();
        if (!args.includedTypes.contains(typeName) || args.excludedTypes.contains(typeName)) {
          toRemove.add(annotationFS);
        }
      }
      for (AnnotationFS each : toRemove) {
        if (!cas.getDocumentAnnotation().equals(each)) {
          cas.removeFsFromIndexes(each);
        }
      }
    }
  }

  private static class Args {

    @Parameter(names = "--view", description = "View to process")
    public String viewCasName = "_InitialView";

    @Parameter(names = "--exclude", description = "Types to exclude, repeat for each type")
    public List<String> excludedTypes = new ArrayList<String>();

    @Parameter(names = "--include", description = "Types to inclue, repeat for each type")
    public List<String> includedTypes = new ArrayList<String>();

    @Parameter(names = "--typesystem", description = "Type system to use", required = true)
    public String typeSystemDescriptorPath = null;

    @Parameter(names = "--eval", description = "Eval folder containing system XMIs", required = true)
    public String systemFolder = null;

    @Parameter(names = "--evaluator", description = "Type of evaluator, one of Exact, PartialMatch, CoreMatch, WordAccuracy, etc")
    public String factoryName = "Exact";

    @Parameter(names = "--gold", description = "Gold folder containing annotated XMIs", required = true)
    public String goldFolder = null;

    @Parameter(names = "--result", description = "Result folder to store result XMIs")
    public String resultFolderName = null;

    public File resultFolder = null;

    @Parameter(names = "--subtypes", description = "Whether to include subtypes")
    public boolean includeSubtypes = false;

    @Parameter(names = "--alltypes", description = "Whether to include all types")
    public boolean useAllTypes = false;

    @Parameter(names = "--csv", description = "File to write per-file CSV")
    public String csvFile = null;

    public List<File> systemFiles = new ArrayList<File>();

    public List<File> goldFiles = new ArrayList<File>();

    public Args() {
    }

    public TypeSystemDescription tsd = null;

    public void expand() throws Exception {
      XMLInputSource in = new XMLInputSource(typeSystemDescriptorPath);
      tsd = UIMAFramework.getXMLParser().parseTypeSystemDescription(in);

      for (File f : new File(systemFolder).listFiles())
        if (f.getName().endsWith(".xmi"))
          systemFiles.add(f);
      for (File f : new File(goldFolder).listFiles())
        if (f.getName().endsWith(".xmi"))
          goldFiles.add(f);

      if (resultFolderName != null)
        resultFolder = new File(resultFolderName);
    }

  }

  private static Args args = new Args();

  public static void main(String[] argv) throws Exception {

    try {
      new JCommander(args, argv);
    } catch (ParameterException e) {
      e.printStackTrace();
      new JCommander(args).usage();
      System.exit(-1);
    }

    args.expand();

    int numFiles = args.goldFiles.size();

    List<TestCasData> testCasData = new ArrayList<TestCasData>(numFiles);
    for (File gold : args.goldFiles)
      testCasData.add(new TestCasData(gold));

    // * for each (goldFile, runFile)-pair:
    CAS runCas = CasCreationUtils.createCas(args.tsd, null, null);
    CAS goldCas = CasCreationUtils.createCas(args.tsd, null, null);
    for (int i = 0; i < numFiles; i++) {
      TestCasData td = testCasData.get(i);
      // init etc
      runCas.reset();
      goldCas.reset();
      // deserialize CASes
      File runFile = args.systemFiles.get(i);
      deserializeCASs(goldCas, td, runCas, runFile);

      runCas = runCas.getView(args.viewCasName);
      goldCas = goldCas.getView(args.viewCasName);
      // ** create TP, FP, FN annotations
      // ** collect results and gather eval data

      ICasEvaluator evaluator = ((ICasEvaluatorFactory) Class.forName(
              "org.ie4opendata.ruta_testing_standalone.testing.evaluator." + args.factoryName
                      + "CasEvaluatorFactory").newInstance()).createEvaluator();

      prepareCas(goldCas);
      prepareCas(runCas);
      CAS resultCas = evaluator.evaluate(goldCas, runCas, args.excludedTypes, args.includeSubtypes,
              args.useAllTypes);

      if (args.resultFolder != null) {
        File resultFile = new File(args.resultFolder, td.getPath().getName()
                .replace(".xmi", ".result.xmi"));
        writeXmi(resultCas, resultFile);
        td.setResultPath(resultFile);
      }

      // finally, calculate eval data and show it in the GUI
      EvalDataProcessor.calculateEvaluatData(td, resultCas);
      resultCas.release();
    }

    runCas.release();
    goldCas.release();

    int falsePositiveTotalCount = 0;
    int falseNegativeTotalCount = 0;
    int truePositiveTotalCount = 0;

    StringBuilder evalData = new StringBuilder("Test File,Type,TP,FP,FN,Recall,Prec,F-1\n");

    for (TestCasData entry : testCasData) {
      @SuppressWarnings("unchecked")
      Collection<TypeEvalData> col = entry.getTypeEvalData().values();
      falsePositiveTotalCount += entry.getFalsePositiveCount();
      falseNegativeTotalCount += entry.getFalseNegativeCount();
      truePositiveTotalCount += entry.getTruePositiveCount();
      for (TypeEvalData data : col) {
        if (!data.getTypeName().equals("Total")) {
          String column = entry.getPath().toString() + "," + data.getTypeName() + ","
                  + String.valueOf(data.getTruePositives()) + ","
                  + String.valueOf(data.getFalsePositives()) + ","
                  + String.valueOf(data.getFalseNegatives()) + ","
                  + String.valueOf(data.getRecall()) + "," + String.valueOf(data.getPrecision())
                  + "," + String.valueOf(data.getFOne()) + "\n";
          evalData.append(column);
        }
      }
    }

    double a = falsePositiveTotalCount;
    double b = falseNegativeTotalCount;
    double c = truePositiveTotalCount;

    double precision = c / (c + a);
    double recall = c / (c + b);
    double fMeasure = 2 * (precision * recall) / (precision + recall);

    fMeasure = fMeasure * 1000;
    fMeasure = Math.round(fMeasure);
    fMeasure = fMeasure / 1000;

    precision = precision * 1000;
    precision = Math.round(precision);
    precision = precision / 1000;

    recall = recall * 1000;
    recall = Math.round(recall);
    recall = recall / 1000;

    System.out.println("Runs\t" + numFiles);
    System.out.println("FP\t" + falsePositiveTotalCount);
    System.out.println("FN\t" + falseNegativeTotalCount);
    System.out.println("TP\t" + truePositiveTotalCount);
    System.out.println("Prec\t" + precision);
    System.out.println("Rec\t" + recall);
    System.out.println("F1\t" + fMeasure);

    if (args.csvFile != null) {
      PrintWriter pw = new PrintWriter(new FileWriter(new File(args.csvFile)));
      pw.println(evalData);
      pw.close();
    }
  }
}

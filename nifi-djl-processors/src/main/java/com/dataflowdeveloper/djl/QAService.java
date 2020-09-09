package com.dataflowdeveloper.djl;

import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.nlp.qa.QAInput;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An example of inference using BertQA.
 *
 * <p>See:
 *
 * <ul>
 *   <li>the <a href="https://github.com/awslabs/djl/blob/master/jupyter/BERTQA.ipynb">jupyter
 *       demo</a> with more information about BERT.
 *   <li>the <a
 *       href="https://github.com/awslabs/djl/blob/master/examples/docs/BERT_question_and_answer.md">docs</a>
 *       for information about running this example.
 * </ul>
 */
// scalastyle:off
// scalastyle:on

public class QAService {

    public String predict() throws IOException, TranslateException, ModelException {
        String question = "When did BBC Japan start broadcasting?";
        String paragraph =
                "BBC Japan was a general entertainment Channel.\n"
                        + "Which operated between December 2004 and April 2006.\n"
                        + "It ceased operations after its Japanese distributor folded.";

        QAInput input = new QAInput(question, paragraph);
        System.out.println("Paragraph: {}" + input.getParagraph());
        System.out.println("Question: {}" + input.getQuestion());

        Criteria<QAInput, String> criteria =
                Criteria.builder()
                        .optApplication(Application.NLP.QUESTION_ANSWER)
                        .setTypes(QAInput.class, String.class)
                        .optFilter("backbone", "bert")
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<QAInput, String> model = ModelZoo.loadModel(criteria)) {
            try (Predictor<QAInput, String> predictor = model.newPredictor()) {
                return predictor.predict(input);
            }
        }
    }
}

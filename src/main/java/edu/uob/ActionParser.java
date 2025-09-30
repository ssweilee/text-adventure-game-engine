package edu.uob;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ActionParser {
    private final LinkedList<GameAction> actions;

    public ActionParser(LinkedList<GameAction> actions) {
        this.actions = actions;
    }

    public void parseAction(File actionsFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(actionsFile);
            document.getDocumentElement().normalize();
            NodeList actionNodes = document.getElementsByTagName("action");

            for (int i = 0; i < actionNodes.getLength(); i++) {
                Element actionElement = (Element) actionNodes.item(i);
                this.parseActionElement(actionElement);
            }
        } catch (Exception e) {
            System.err.println("Error parsing actions file: ");
            e.printStackTrace();
        }
    }

    private void parseActionElement(Element actionElement) {
        List<String> triggers = this.parseElements(actionElement, "triggers", "keyphrase");
        List<String> subjects = this.parseElements(actionElement, "subjects", "entity");
        List<String> consumed = this.parseElements(actionElement, "consumed", "entity");
        List<String> produced = this.parseElements(actionElement, "produced", "entity");
        String narration = (actionElement.getElementsByTagName("narration").item(0)).getTextContent();

        GameAction action = new GameAction(triggers, subjects, consumed, produced, narration);
        this.actions.add(action);
    }

    private List<String> parseElements(Element parent, String tagName, String childTag) {
        LinkedList<String> result = new LinkedList<>();
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            NodeList childNodes = element.getElementsByTagName(childTag);
            int index = 0;
            while (index < childNodes.getLength()) {
                result.add(childNodes.item(index).getTextContent());
                index++;
            }
        }
        return result;
    }
}
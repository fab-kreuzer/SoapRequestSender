package de.fabkreuzer.soaprequestsender.ui.component;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A custom JTextPane with XML syntax highlighting capabilities.
 * This component highlights XML tags, attributes, and values with different colors.
 */
public class XmlTextPane extends JTextPane {
    
    // Styles for different XML elements
    private Style tagStyle;
    private Style attributeStyle;
    private Style valueStyle;
    private Style commentStyle;
    private Style defaultStyle;
    
    // Patterns for XML elements
    private static final Pattern TAG_PATTERN = Pattern.compile("</?[\\w\\:\\-]+|>");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\s+[\\w\\:\\-]+=");
    private static final Pattern VALUE_PATTERN = Pattern.compile("\"[^\"]*\"|'[^']*'");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--.*?-->");
    
    /**
     * Creates a new XmlTextPane with XML syntax highlighting.
     */
    public XmlTextPane() {
        // Initialize the styles
        initStyles();
        
        // Add a document listener to update highlighting when text changes
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> highlightXml());
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> highlightXml());
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                // Plain text components don't fire these events
            }
        });
        
        // Set the font to monospaced for better readability
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }
    
    /**
     * Initialize the styles for XML elements.
     */
    private void initStyles() {
        StyledDocument doc = getStyledDocument();
        
        // Default style (black)
        defaultStyle = doc.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, Color.BLACK);
        
        // Tag style (blue)
        tagStyle = doc.addStyle("tag", null);
        StyleConstants.setForeground(tagStyle, new Color(0, 0, 200));
        StyleConstants.setBold(tagStyle, true);
        
        // Attribute style (green)
        attributeStyle = doc.addStyle("attribute", null);
        StyleConstants.setForeground(attributeStyle, new Color(0, 128, 0));
        
        // Value style (red)
        valueStyle = doc.addStyle("value", null);
        StyleConstants.setForeground(valueStyle, new Color(200, 0, 0));
        
        // Comment style (gray)
        commentStyle = doc.addStyle("comment", null);
        StyleConstants.setForeground(commentStyle, Color.GRAY);
        StyleConstants.setItalic(commentStyle, true);
    }
    
    /**
     * Highlight XML syntax in the document.
     */
    public void highlightXml() {
        StyledDocument doc = getStyledDocument();
        String text;
        
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            return;
        }
        
        // Reset all styles to default
        doc.setCharacterAttributes(0, text.length(), defaultStyle, true);
        
        // Highlight comments
        highlightPattern(text, COMMENT_PATTERN, doc, commentStyle);
        
        // Highlight tags
        highlightPattern(text, TAG_PATTERN, doc, tagStyle);
        
        // Highlight attributes
        highlightPattern(text, ATTRIBUTE_PATTERN, doc, attributeStyle);
        
        // Highlight values
        highlightPattern(text, VALUE_PATTERN, doc, valueStyle);
    }
    
    /**
     * Highlight all occurrences of a pattern in the text with the specified style.
     */
    private void highlightPattern(String text, Pattern pattern, StyledDocument doc, Style style) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
        }
    }
    
    /**
     * Set the XML content of the text pane and highlight it.
     */
    public void setXmlContent(String xml) {
        setText(xml);
        highlightXml();
    }
    
    /**
     * Get the XML content of the text pane.
     */
    public String getXmlContent() {
        return getText();
    }
}
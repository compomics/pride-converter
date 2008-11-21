/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 23-Aug-2006
 * Time: 10:32:23
 */
package uk.ac.ebi.tpp_to_pride.parsers;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
/*
 * CVS information:
 *
 * $Revision: 1.1.1.1 $
 * $Date: 2007/01/12 17:17:10 $
 */

/**
 * This class provides some XmlPullParser extensions through a
 * standard decorator pattern.
 *
 * @author martlenn
 * @version $Id: XmlPullParserPlus.java,v 1.1.1.1 2007/01/12 17:17:10 lmartens Exp $
 */
public class XmlPullParserPlus {

    /**
     * The wrapped XmlPullParser.
     */
    private XmlPullParser parser = null;

    /**
     * This constructor takes the XmlPullParser instance to wrap.
     *
     * @param aParser XmlPullParser to wrap.
     */
    public XmlPullParserPlus(XmlPullParser aParser) {
        parser = aParser;
    }

    /**
     * This method sets a new XmlPullParser instance to wrap.
     *
     * @param aParser XmlPullParser to wrap.
     */
    public void setXmlPullParser(XmlPullParser aParser) {
        parser = aParser;
    }

    /**
     * This method moves the XmlPullParser forward towards the next start tag,
     * or until an END_DOCUMENT is encountered - whichever comes first.
     *
     * @param aAdvanceFirst boolean to indicate whether the parser needs to move
     *                      one position forward before checking for a start tag.
     * @throws XmlPullParserException   when the XML parsing generated an error.
     * @throws IOException  when the output file could not be read.
     */
    protected void moveToNextStartTag(boolean aAdvanceFirst) throws XmlPullParserException, IOException {
        if(aAdvanceFirst) {
            parser.next();
        }
        // Stop plowing through the tags if we find a start tag OR
        // if we find the end of the document!
        while( (parser.getEventType() != XmlPullParser.START_TAG) && (parser.getEventType() != XmlPullParser.END_DOCUMENT)) {
            parser.next();
        }
    }

    /**
     * This method moves the XmlPullParser forward towards the next start tag,
     * or until the specified end tag is encountered - whichever comes first.
     *
     * @param aBreakingEndTag String that specifies the name for the overriding end tag.
     * @throws XmlPullParserException   when the XML parsing generated an error.
     * @throws IOException  when the output file could not be read.
     */
    protected void moveToNextStartTagWithEndBreaker(String aBreakingEndTag) throws XmlPullParserException, IOException {
        parser.next();
        // Stop plowing through the tags if we find a start tag OR
        // if we find the end tag specified!
        while( (parser.getEventType() != XmlPullParser.START_TAG) && !(parser.getEventType() == XmlPullParser.END_TAG && aBreakingEndTag.equals(parser.getName()))) {
            parser.next();
        }
    }

    /**
     * This method moves the XmlPullParser forward towards the first start tag
     * after the specified end tag, or until an END_DOCUMENT is encountered -
     * whichever comes first.
     *
     * @param aAfterEndTag  String with the name of the end tag that needs to be encountered first.
     * @throws XmlPullParserException   when the XML parsing generated an error.
     * @throws IOException  when the output file could not be read.
     */
    protected void moveToNextStartTagAfterTag(String aAfterEndTag) throws XmlPullParserException, IOException {
        parser.next();
        boolean endTagFound = false;
        while((!(endTagFound && parser.getEventType() == XmlPullParser.START_TAG)) && (parser.getEventType() != XmlPullParser.END_DOCUMENT) ) {
            if(parser.getEventType() == XmlPullParser.END_TAG && aAfterEndTag.equals(parser.getName())) {
                endTagFound = true;
            }
            parser.next();
        }
    }

    public void setFeature(String s, boolean b) throws XmlPullParserException {
        parser.setFeature(s, b);
    }

    public boolean getFeature(String s) {
        return parser.getFeature(s);
    }

    public void setProperty(String s, Object o) throws XmlPullParserException {
        parser.setProperty(s, o);
    }

    public Object getProperty(String s) {
        return parser.getProperty(s);
    }

    public void setInput(Reader aReader) throws XmlPullParserException {
        parser.setInput(aReader);
    }

    public void setInput(InputStream aInputStream, String s) throws XmlPullParserException {
        parser.setInput(aInputStream, s);
    }

    public String getInputEncoding() {
        return parser.getInputEncoding();
    }

    public void defineEntityReplacementText(String s, String s1) throws XmlPullParserException {
        parser.defineEntityReplacementText(s, s1);
    }

    public int getNamespaceCount(int i) throws XmlPullParserException {
        return parser.getNamespaceCount(i);
    }

    public String getNamespacePrefix(int i) throws XmlPullParserException {
        return parser.getNamespacePrefix(i);
    }

    public String getNamespaceUri(int i) throws XmlPullParserException {
        return parser.getNamespaceUri(i);
    }

    public String getNamespace(String s) {
        return parser.getNamespace(s);
    }

    public int getDepth() {
        return parser.getDepth();
    }

    public String getPositionDescription() {
        return parser.getPositionDescription();
    }

    public int getLineNumber() {
        return parser.getLineNumber();
    }

    public int getColumnNumber() {
        return parser.getColumnNumber();
    }

    public boolean isWhitespace() throws XmlPullParserException {
        return parser.isWhitespace();
    }

    public String getText() {
        return parser.getText();
    }

    public char[] getTextCharacters(int[] aInts) {
        return parser.getTextCharacters(aInts);
    }

    public String getNamespace() {
        return parser.getNamespace();
    }

    public String getName() {
        return parser.getName();
    }

    public String getPrefix() {
        return parser.getPrefix();
    }

    public boolean isEmptyElementTag() throws XmlPullParserException {
        return parser.isEmptyElementTag();
    }

    public int getAttributeCount() {
        return parser.getAttributeCount();
    }

    public String getAttributeNamespace(int i) {
        return parser.getAttributeNamespace(i);
    }

    public String getAttributeName(int i) {
        return parser.getAttributeName(i);
    }

    public String getAttributePrefix(int i) {
        return parser.getAttributePrefix(i);
    }

    public String getAttributeType(int i) {
        return parser.getAttributeType(i);
    }

    public boolean isAttributeDefault(int i) {
        return parser.isAttributeDefault(i);
    }

    public String getAttributeValue(int i) {
        return parser.getAttributeValue(i);
    }

    public String getAttributeValue(String s, String s1) {
        return parser.getAttributeValue(s, s1);
    }

    public int getEventType() throws XmlPullParserException {
        return parser.getEventType();
    }

    public int next() throws XmlPullParserException, IOException {
        return parser.next();
    }

    public int nextToken() throws XmlPullParserException, IOException {
        return parser.nextToken();
    }

    public void require(int i, String s, String s1) throws XmlPullParserException, IOException {
        parser.require(i, s, s1);
    }

    public String nextText() throws XmlPullParserException, IOException {
        return parser.nextText();
    }

    public int nextTag() throws XmlPullParserException, IOException {
        return parser.nextTag();
    }
}

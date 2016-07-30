package dominus.framework.binding;


import dominus.framework.binding.bean.PersonType;
import dominus.framework.junit.DominusJUnit4TestBase;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;


/**
 * EE: Contract First
 * When using contract-first, you start with the WSDL contract, and use Java to implement said contract.
 * 1, XML Document;
 * 2, Generate XSD schema from XML document;
 * 3, Use XML2JavaCompiler(jxc) to generate java classes;
 */
public class TestXmlBinding extends DominusJUnit4TestBase {

    String personXml;
    String invalidPersonXml;

    @Override
    protected void doSetUp() throws Exception {
        personXml = IOUtils.toString(resourceLoader.getResource("classpath:data/xml/Person.xml").getURI(), "UTF-8");
        invalidPersonXml = IOUtils.toString(resourceLoader.getResource("classpath:data/xml/InvalidPerson.xml").getURI(), "UTF-8");
        System.out.println("[JUnit].setUp\n" + personXml);
        System.out.println("[JUnit].setUp\n" + invalidPersonXml);
    }

    @Test
    public void testSimpleUnmarshal() {
        PersonType person = JAXB.unmarshal(new StringReader(personXml), PersonType.class);
        assertEquals(person.getFirstName(), "Shawn");
        assertEquals(person.getLastName(), "Guo");
    }

    @Test(expected = javax.xml.bind.UnmarshalException.class)
    public void testSchemaValidation() throws SAXException, JAXBException, IOException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(resourceLoader.getResource("classpath:data/xml/Person.xsd").getURL());
        Unmarshaller unmarshaller = JAXBContext.newInstance(PersonType.class).createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new MyValidationEventHandler());
        PersonType person = (PersonType) unmarshaller.unmarshal(new StringReader(invalidPersonXml));
    }

    public void testDataTypeConversion() {

    }


    //TODO schema validation, multiple xml body, complex xml
    //Contract-First Web Services

    public static class MyValidationEventHandler implements ValidationEventHandler {

        public boolean handleEvent(ValidationEvent event) {
            System.out.println("\nEVENT");
            System.out.println("SEVERITY:  " + event.getSeverity());
            System.out.println("MESSAGE:  " + event.getMessage());
            System.out.println("LINKED EXCEPTION:  " + event.getLinkedException());
            System.out.println("LOCATOR");
            System.out.println("    LINE NUMBER:  " + event.getLocator().getLineNumber());
            System.out.println("    COLUMN NUMBER:  " + event.getLocator().getColumnNumber());
            System.out.println("    OFFSET:  " + event.getLocator().getOffset());
            System.out.println("    OBJECT:  " + event.getLocator().getObject());
            System.out.println("    NODE:  " + event.getLocator().getNode());
            System.out.println("    URL:  " + event.getLocator().getURL());
            return true;
        }

    }
}

package ru.fizteh.fivt.students.anastasyev.filemap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.IdentityHashMap;

public class MyInvocationHandler implements InvocationHandler {
    private Writer writer;
    private Object implementation;
    private XMLStreamWriter xmlStreamWriter;
    private IdentityHashMap<Object, Boolean> discoveredItems = new IdentityHashMap<>();

    private void writeList(Iterable list) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("list");
        for (Object element : list) {
            xmlStreamWriter.writeStartElement("value");
            if (element == null) {
                xmlStreamWriter.writeEmptyElement("null");
            } else {
                if (element instanceof Iterable) {
                    if (discoveredItems.containsKey(element) && ((Iterable) element).iterator().hasNext()) {
                        xmlStreamWriter.writeCharacters("cyclic");
                    } else {
                        discoveredItems.put(element, true);
                        writeList((Iterable) element);
                    }
                } else {
                    xmlStreamWriter.writeCharacters(element.toString());
                }
            }
            xmlStreamWriter.writeEndElement();
        }
        xmlStreamWriter.writeEndElement();
    }

    private void writeArg(Object arg) throws XMLStreamException {
        xmlStreamWriter.writeStartElement("argument");
        if (arg == null) {
            xmlStreamWriter.writeEmptyElement("null");
        } else if (arg instanceof Iterable) {
            writeList((Iterable) arg);
            discoveredItems.clear();
        } else {
            xmlStreamWriter.writeCharacters(arg.toString());
        }
        xmlStreamWriter.writeEndElement();
    }

    private void writeLog(Object[] args) throws XMLStreamException {
        if (args == null || args.length == 0) {
            xmlStreamWriter.writeEmptyElement("arguments");
        } else {
            xmlStreamWriter.writeStartElement("arguments");
            for (Object arg : args) {
                writeArg(arg);
            }
            xmlStreamWriter.writeEndElement();
        }
    }

    public MyInvocationHandler(Writer newWriter, Object newImplementation) {
        writer = newWriter;
        implementation = newImplementation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(implementation, args);
        }
        Object result = null;

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        StringWriter stringWriter = new StringWriter();
        xmlStreamWriter = factory.createXMLStreamWriter(stringWriter);

        xmlStreamWriter.writeStartElement("invoke");
        xmlStreamWriter.writeAttribute("timestamp", Long.toString(System.currentTimeMillis()));
        xmlStreamWriter.writeAttribute("class", implementation.getClass().getName());
        xmlStreamWriter.writeAttribute("name", method.getName());

        writeLog(args);

        try {
            result = method.invoke(implementation, args);
            if (!method.getReturnType().equals(void.class)) {
                xmlStreamWriter.writeStartElement("return");
                if (result != null) {
                    xmlStreamWriter.writeCharacters(result.toString());
                } else {
                    xmlStreamWriter.writeEmptyElement("null");
                }
                xmlStreamWriter.writeEndElement();
            }
        } catch (InvocationTargetException e) {
            xmlStreamWriter.writeStartElement("thrown");
            xmlStreamWriter.writeCharacters(e.getClass().getName() + ": " + e.getMessage());
            xmlStreamWriter.writeEndElement();
        } finally {
            xmlStreamWriter.writeEndElement();
            xmlStreamWriter.flush();
            writer.write(stringWriter.toString() + System.getProperty("line.separator"));
        }
        return result;
    }
}

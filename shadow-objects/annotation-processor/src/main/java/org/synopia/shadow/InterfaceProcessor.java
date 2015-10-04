package org.synopia.shadow;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by synopia on 27.09.2015.
 */
@SupportedAnnotationTypes({"org.synopia.shadow.Parameter", "org.synopia.shadow.ReadonlyParameter"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({"packageName", "prefix"})
public class InterfaceProcessor extends AbstractProcessor {

    private Map<Element, List<Element>> elements;
    private Set<Element> readonly = new HashSet<>();
    private Set<String> types;
    private String packageName;
    private String prefix;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        Map<String, String> options = processingEnv.getOptions();
        packageName = options.getOrDefault("packageName", "default");
        prefix = options.getOrDefault("prefix", "S");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        elements = new HashMap<>();
        types = new HashSet<>();


        for (TypeElement annotation : annotations) {
            boolean ro = annotation.getSimpleName().toString().equals("ReadonlyParameter");
            Set<? extends Element> entities = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : entities) {
                Element type = element.getEnclosingElement();
                List<Element> parameters = elements.get(type);
                if (parameters == null) {
                    parameters = new ArrayList<>();
                    elements.put(type, parameters);
                    types.add(type.asType().toString());
                }
                parameters.add(element);
                if (ro) {
                    readonly.add(element);
                }
            }
        }

        for (Map.Entry<Element, List<Element>> entry : elements.entrySet()) {
            try {
                writeInterface(entry.getKey(), entry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void writeInterface(Element type, List<Element> parameters) throws IOException {
        String className = type.getSimpleName().toString();
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(packageName + "." + prefix + className);
        PrintWriter out = new PrintWriter(sourceFile.openWriter());
        out.println("package " + packageName + ";");
        out.println("public interface " + prefix + className + " extends org.synopia.shadow.ShadowObject {");
        for (Element parameter : parameters) {
            VariableElement v = (VariableElement) parameter;
            TypeMirror type1 = v.asType();
            TypeKind kind = type1.getKind();
            String typeName = type1.toString();
            boolean ro = readonly.contains(parameter);
            if (typeName.startsWith("java.util.List") && !ro) {
                String listType = listType(type1);
                writeListGetter(out, listType, v.getSimpleName().toString());
                writeListSetter(out, listType, v.getSimpleName().toString());
            } else if (kind.isPrimitive() || typeName.equals("java.lang.String")) {
                writeGetter(out, typeName, v.getSimpleName().toString());
                if (!ro) writeSetter(out, typeName, v.getSimpleName().toString());
            } else if (kind == TypeKind.DECLARED) {
                Element paramTypeElement = ((DeclaredType) type1).asElement();
                ElementKind typeKind = paramTypeElement.getKind();
                if (typeKind == ElementKind.ENUM) {
                    writeGetter(out, typeName, v.getSimpleName().toString());
                    if (!ro) writeSetter(out, typeName, v.getSimpleName().toString());
                } else {
                    if (types.contains(typeName)) {
                        String s = typeName.substring(typeName.lastIndexOf('.') + 1);
                        s = packageName + "." + prefix + s;
                        writeGetter(out, s, v.getSimpleName().toString());
                        if (!ro) writeSetter(out, s, v.getSimpleName().toString());
                    } else {
                        writeGetter(out, typeName, v.getSimpleName().toString());
                        if (!ro) writeSetter(out, typeName, v.getSimpleName().toString());
                    }
                }
            } else if (kind == TypeKind.ARRAY) {
                writeGetter(out, typeName, v.getSimpleName().toString());
                if (!ro) writeSetter(out, typeName, v.getSimpleName().toString());
            }
        }
        out.println("}");
        out.close();
    }

    private String listType(TypeMirror type) {
        String s = type.toString();
        String listType = s.substring(s.indexOf('<') + 1, s.indexOf('>'));
        switch (listType) {
            case "java.lang.Float":
                return "float";
            case "java.lang.Integer":
                return "int";
            default: {
                if (types.contains(listType)) {

                    s = listType.substring(listType.lastIndexOf('.') + 1);
                    listType = packageName + "." + prefix + s;
                }

                return listType;
            }
        }
    }

    private void writeListSetter(PrintWriter out, String type, String name) {
        out.println("    " + "void set" + firstUp(name) + "(int index, " + type + " " + name + ");");
    }

    private void writeListGetter(PrintWriter out, String type, String name) {
        out.println("    " + type + " get" + firstUp(name) + "(int index);");
        out.println("    " + "int " + name + "Count();");
    }

    private void writeSetter(PrintWriter out, String type, String name) {
        out.println("    " + "void set" + firstUp(name) + "(" + type + " " + name + ");");
    }

    private void writeGetter(PrintWriter out, String type, String name) {
        out.println("    " + type + " get" + firstUp(name) + "();");
    }

    private String firstUp(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

}

package org.eclipse.etools.ei18n.extensions;

public class ImpexExtension {
    private final String id;
    private final String name;
    private final IImpex impexApplication;
    private final String fileExtension;

    public ImpexExtension(String id, String name, String fileExtension, IImpex impexApplication) {
        this.id=id;
        this.name=name;
        this.fileExtension=fileExtension;
        this.impexApplication=impexApplication;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public IImpex getApplication() {
        return impexApplication;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}

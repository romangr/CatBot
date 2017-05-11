package Utils;

/**
 * Roman 27.10.2016.
 */
public class URLBuilder {

    private StringBuilder builder = new StringBuilder();
    private boolean hostDefinded = false;
    private boolean parametersDefined = false;

    public String build() {
        return builder.toString();
    }

    public URLBuilder withHost(String host) {
        if (!hostDefinded) {
            builder.append(host);
        } else {
            throw new IllegalStateException("Host is already defined");
        }
        hostDefinded = true;
        return this;
    }

    public URLBuilder withPath(String path) {
        if (!hostDefinded)
            throw new IllegalStateException("Host is not defined!");
        if (parametersDefined)
            throw new IllegalStateException("Define path before parameters!");

        builder.append("/")
                .append(path);
        return this;
    }

    public URLBuilder withParameter(String parameter, Object value) {
        if (!hostDefinded)
            throw new IllegalStateException("Host is not defined!");

        String prefix = parametersDefined ? "&" : "?";
        builder.append(prefix)
                .append(parameter)
                .append("=")
                .append(value.toString());
        parametersDefined = true;
        return this;
    }


}

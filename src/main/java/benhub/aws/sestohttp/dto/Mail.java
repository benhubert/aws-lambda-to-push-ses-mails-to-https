package benhub.aws.sestohttp.dto;

import java.util.List;
import java.util.Optional;

public class Mail {
    private List<Header> headers;

    public List<Header> getHeaders() {
        return headers;
    }

    public Optional<Header> getHeader(String name) {
        if (headers != null) {
            return headers.stream()
                    .filter(h -> name.equalsIgnoreCase(h.getName()))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }
}

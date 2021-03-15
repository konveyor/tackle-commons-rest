import io.tackle.commons.annotations.Filterable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

public class FilterNameMissingEntity {
    @OneToMany
    @Filterable
    public List<String> fields = new ArrayList<>();
}

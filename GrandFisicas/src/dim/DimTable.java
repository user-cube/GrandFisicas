package dim;
import java.util.*;

public class DimTable{

	public Set<Unidade> table = new TreeSet<>();

    public boolean exists(Unidade grandeza){
        assert (grandeza!=null);
        return table.contains(grandeza);
    }

    public void put(Unidade grandeza){
        assert (grandeza!=null);

        table.add(grandeza);
    }

}
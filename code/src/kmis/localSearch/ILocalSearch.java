package kmis.localSearch;

import kmis.structure.Instance;
import kmis.structure.Solution;

public interface ILocalSearch {

    Solution execute(Solution sol, Instance instance);
    String toString();
}

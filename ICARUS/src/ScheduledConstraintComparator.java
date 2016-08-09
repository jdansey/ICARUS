import java.util.Comparator;

public class ScheduledConstraintComparator implements Comparator<ScheduledConstraint> {

	public ScheduledConstraintComparator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(ScheduledConstraint arg0, ScheduledConstraint arg1) {
		if(arg0.getCellPhone().getMainPriority() < arg1.getCellPhone().getMainPriority()){
			return -1;
		}else if(arg0.getCellPhone().getMainPriority() == arg1.getCellPhone().getMainPriority()){
			if(arg0.getCellPhone().getSubPriority() <= arg1.getCellPhone().getSubPriority()){
				return -1;
			}else{
				return 1;
			}
		}else{
			return 1;
		}
	}

}

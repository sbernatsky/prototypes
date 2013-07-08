package proto.action.spring;

final class ActionMapping {
    private final String name;

    public ActionMapping(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ActionMapping)) {
            return false;
        }

        return name.equals(((ActionMapping) obj).name);
    }

    @Override
    public String toString() {
        return name;
    }
}

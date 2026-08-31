class SequentialIds implements IdSource {
    private long counter = 0;

    @Override
    public String next() {
        return "ID-" + (++counter);
    }
}

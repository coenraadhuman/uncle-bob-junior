class DurationTest {
    
    @Test void throwsOnInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> Duration.parse("30x"));
    }
    
    @Test void providesSecondAccessor() {
        Duration d = Duration.parse("2m");
        assertEquals(120, d.seconds());
    }
}

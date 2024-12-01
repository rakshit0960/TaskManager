public class ProcessInfo {
    private long pid;
    private String name;
    private long memoryUsage;
    private double cpuUsage;

    public ProcessInfo(long pid, String name, long memoryUsage, double cpuUsage) {
        this.pid = pid;
        this.name = name;
        this.memoryUsage = memoryUsage;
        this.cpuUsage = cpuUsage;
    }

    public long getPid() { return pid; }
    public String getName() { return name; }
    public long getMemoryUsage() { return memoryUsage; }
    public double getCpuUsage() { return cpuUsage; }
}
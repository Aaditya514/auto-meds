package com.automeds.dto;

/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AdminDashboardDTO
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class AdminDashboardDTO {

    private Long totalPatients;
    private Long totalMedicines;
    private Long activeSubscriptions;
    private Long pendingRequests;
    private Long upcomingRefills;
    private Long pendingOrders;
    private Long lowStockMedicines;
    private Long outOfStockMedicines;
    private Long deficitSubscriptions = 0L;
    private Long procurementAlerts = 0L;

    public AdminDashboardDTO() {
    }

    @SuppressWarnings("java:S107")
    public AdminDashboardDTO(Long totalPatients, Long totalMedicines, Long activeSubscriptions, Long pendingRequests, Long upcomingRefills, Long pendingOrders, Long lowStockMedicines, Long outOfStockMedicines) {
        this(totalPatients, totalMedicines, activeSubscriptions, pendingRequests, upcomingRefills, pendingOrders, lowStockMedicines, outOfStockMedicines, 0L, 0L);
    }

    public AdminDashboardDTO(Long totalPatients, Long totalMedicines, Long activeSubscriptions, Long pendingRequests, Long upcomingRefills, Long pendingOrders, Long lowStockMedicines, Long outOfStockMedicines, Long deficitSubscriptions, Long procurementAlerts) {
        this.totalPatients = totalPatients;
        this.totalMedicines = totalMedicines;
        this.activeSubscriptions = activeSubscriptions;
        this.pendingRequests = pendingRequests;
        this.upcomingRefills = upcomingRefills;
        this.pendingOrders = pendingOrders;
        this.lowStockMedicines = lowStockMedicines;
        this.outOfStockMedicines = outOfStockMedicines;
        this.deficitSubscriptions = deficitSubscriptions != null ? deficitSubscriptions : 0L;
        this.procurementAlerts = procurementAlerts != null ? procurementAlerts : 0L;
    }

    public Long getDeficitSubscriptions() {
        return deficitSubscriptions;
    }

    public void setDeficitSubscriptions(Long deficitSubscriptions) {
        this.deficitSubscriptions = deficitSubscriptions;
    }

    public Long getProcurementAlerts() {
        return procurementAlerts;
    }

    public void setProcurementAlerts(Long procurementAlerts) {
        this.procurementAlerts = procurementAlerts;
    }

    public Long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(Long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public Long getTotalMedicines() {
        return totalMedicines;
    }

    public void setTotalMedicines(Long totalMedicines) {
        this.totalMedicines = totalMedicines;
    }

    public Long getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void setActiveSubscriptions(Long activeSubscriptions) {
        this.activeSubscriptions = activeSubscriptions;
    }

    public Long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(Long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public Long getUpcomingRefills() {
        return upcomingRefills;
    }

    public void setUpcomingRefills(Long upcomingRefills) {
        this.upcomingRefills = upcomingRefills;
    }

    public Long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Long getLowStockMedicines() {
        return lowStockMedicines;
    }

    public void setLowStockMedicines(Long lowStockMedicines) {
        this.lowStockMedicines = lowStockMedicines;
    }

    public Long getOutOfStockMedicines() {
        return outOfStockMedicines;
    }

    public void setOutOfStockMedicines(Long outOfStockMedicines) {
        this.outOfStockMedicines = outOfStockMedicines;
    }

    @Override
    public String toString() {
        return "AdminDashboardDTO{" +
                "totalPatients=" + totalPatients +
                ", totalMedicines=" + totalMedicines +
                ", activeSubscriptions=" + activeSubscriptions +
                ", pendingRequests=" + pendingRequests +
                ", upcomingRefills=" + upcomingRefills +
                ", pendingOrders=" + pendingOrders +
                ", lowStockMedicines=" + lowStockMedicines +
                ", outOfStockMedicines=" + outOfStockMedicines +
                '}';
    }
}

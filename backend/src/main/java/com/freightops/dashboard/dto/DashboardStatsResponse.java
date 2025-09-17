package com.freightops.dashboard.dto;

public class DashboardStatsResponse {
    private Long totalLTAs;
    private Long activeLTAs;
    private Long inTransitLTAs;
    private Long deliveredLTAs;
    private Double totalRevenue;
    private Double monthlyRevenue;

    public DashboardStatsResponse() {
    }

    public Long getTotalLTAs() {
        return totalLTAs;
    }

    public void setTotalLTAs(Long totalLTAs) {
        this.totalLTAs = totalLTAs;
    }

    public Long getActiveLTAs() {
        return activeLTAs;
    }

    public void setActiveLTAs(Long activeLTAs) {
        this.activeLTAs = activeLTAs;
    }

    public Long getInTransitLTAs() {
        return inTransitLTAs;
    }

    public void setInTransitLTAs(Long inTransitLTAs) {
        this.inTransitLTAs = inTransitLTAs;
    }

    public Long getDeliveredLTAs() {
        return deliveredLTAs;
    }

    public void setDeliveredLTAs(Long deliveredLTAs) {
        this.deliveredLTAs = deliveredLTAs;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(Double monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }
}

package bg.sofia.uni.fmi.mjt.food.analyzer.server.dto;

public class FoodProduct {
    private int fdcId;

    private String description;

    private String dataType;

    private String gtinUpc;

    private String publishedDate;

    private String brandOwner;

    private String ingredients;

    private double score;

    public FoodProduct(int fdcId, String description, String dataType, String gtinUpc, String publishedDate,
                       String brandOwner, String ingredients, double score) {
        this.fdcId = fdcId;
        this.description = description;
        this.dataType = dataType;
        this.gtinUpc = gtinUpc;
        this.publishedDate = publishedDate;
        this.brandOwner = brandOwner;
        this.ingredients = ingredients;
        this.score = score;
    }

    public int getFdcId() {
        return fdcId;
    }

    public void setFdcId(int fdcId) {
        this.fdcId = fdcId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getGtinUpc() {
        return gtinUpc;
    }

    public void setGtinUpc(String gtinUpc) {
        this.gtinUpc = gtinUpc;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getBrandOwner() {
        return brandOwner;
    }

    public void setBrandOwner(String brandOwner) {
        this.brandOwner = brandOwner;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return fdcId +
            "&&" + description +
            "&&" + dataType +
            "&&" + gtinUpc +
            "&&" + publishedDate +
            "&&" + brandOwner +
            "&&" + ingredients +
            "&&" + score +
             System.lineSeparator().toString();
    }

    public String humanReadable() {
        return String.format("{\n\tfdcId: %d\n\tdescription: %s\n\tgtinUpc: %s\n\tpublishedDate: %s\n\t" +
            "brandOwner: %s\n\tingredients: %s\n\tscore: %.2f\n}\n", fdcId, description, gtinUpc, publishedDate,
            brandOwner, ingredients, score);
    }
}

package de.in.tum.compare;

public class Query {

	private String teamName;
	private String url;

	public Query(String teamName, String url) {
		super();
		this.teamName = teamName;
		this.url = url;
	}

	/**
	 * @return the teamName
	 */
	public String getTeamName() {
		return teamName;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

}

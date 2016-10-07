package prelich.curiomobile.dummy;


import android.graphics.drawable.Drawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import prelich.curiomobile.ItemListActivity;

public class ProjectContent {

    public static JSONArray projectsArray;
    public static JSONArray questionsArray;

    public static final List<Project> ITEMS = new ArrayList<Project>();             // An array of project items.
    public static final Map<String, Project> ITEM_MAP = new HashMap<String, Project>();     // A map of project items, by ID.

    public static void updateItems()  {
        ITEMS.clear();
        ITEM_MAP.clear();
        for(int i = 0; i < projectsArray.length(); i++) {
            try {
                JSONObject jsonProject = projectsArray.getJSONObject(i);
                int id = jsonProject.getInt("id");

                InputStream is = (InputStream) new URL(jsonProject.getString("avatar")).getContent();
                Drawable headerImage = Drawable.createFromStream(is, "Avatar_" + i);

                // Add Research Questions
                ArrayList<ResearchQuestion> questionsList = new ArrayList<>();
                for(int j = 0; j < questionsArray.length(); j++) {
                    JSONObject jsonQuestion = questionsArray.getJSONObject(j);
                    if(id == jsonQuestion.getInt("project")) {
                        ResearchQuestion researchQuestion = new ResearchQuestion(
                                jsonQuestion.getInt("id"),
                                jsonQuestion.getString("title"),
                                jsonQuestion.getString("question"),
                                jsonQuestion.getString("motivation") );
                        questionsList.add(researchQuestion);
                    }
                }

                // Add Team Members
                ArrayList<Member> memberList = new ArrayList<>();
                JSONArray teamArray = jsonProject.getJSONArray("team");
                for(int j = 0; j < teamArray.length(); j++) {
                    JSONObject jsonMember = teamArray.getJSONObject(j);
                    int member_id = jsonMember.getInt("id");

                    // GET member profile JSON Object
                    URL userURL = new URL("http://test.crowdcurio.com/api/user/profile/" + member_id);
                    String inputStr;
                    HttpURLConnection urlConnection = null;
                    StringBuilder responseStrBuilder = new StringBuilder();
                    urlConnection = (HttpURLConnection) userURL.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);

                    JSONObject jsonMemberDetails = new JSONObject(String.valueOf(responseStrBuilder));

                    is = (InputStream) new URL(jsonMemberDetails.getString("avatar")).getContent();
                    Drawable avatarImage = Drawable.createFromStream(is, "Avatar_" + i);

                    Member member = new Member(
                            member_id,
                            jsonMemberDetails.getString("owner"),
                            jsonMemberDetails.getString("nickname"),
                            jsonMemberDetails.getString("email"),
                            avatarImage,
                            jsonMemberDetails.getString("bio"),
                            jsonMemberDetails.getString("title") );
                    memberList.add(member);
                }

                // Create and add Project
                Project project = new Project(String.valueOf(i),
                        jsonProject.getString("name"),
                        headerImage,
                        jsonProject.getString("short_description"),
                        jsonProject.getString("description"),
                        questionsList,
                        memberList);

                // If there is a search query, be sure to hide appropriately
                if (!ItemListActivity.mSearchString.toString().isEmpty()) {
                    if(project.description.toLowerCase().contains(ItemListActivity.mSearchString.toString().toLowerCase()) ||
                            project.name.toLowerCase().contains(ItemListActivity.mSearchString.toString().toLowerCase()) ) {
                        project.show =  true;
                    }
                    else {
                        project.show =  false;
                    }
                }

                addItem(project);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addItem(Project item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static void removeItem(Project item) {
        ITEMS.remove(item);
    }

    // Project class to hold each project's data
    public static class Project {
        public String id = null;

        public String name = null;
        public Drawable avatar = null;
        public String short_description = null;
        public String description = null;
        public ArrayList<ResearchQuestion> questions = null;
        public ArrayList<Member> members = null;
        public boolean show = true; // Used for search

        public Project(String id, String name, Drawable avatar, String short_description, String description, ArrayList<ResearchQuestion> questions, ArrayList<Member> members) {
            this.id = id;
            this.name = name;
            this.avatar = avatar;
            this.short_description = short_description;
            this.description = description;
            this.questions = questions;
            this.members = members;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class ResearchQuestion {
        public int id= -1;
        public String title = null;
        public String question = null;
        public String motivation = null;

        public ResearchQuestion(int id, String title, String question, String motivation) {
            this.id = id;
            this.title = title;
            this.question = question;
            this.motivation = motivation;
        }
    }

    public static class Member {
        public int id = -1;
        public String owner = null;
        public  String nickname = null;
        public String email = null;
        public Drawable avatar = null;
        public String bio = null;
        public String title = null;

        public Member(int id, String owner, String nickname, String email, Drawable avatar, String bio, String title) {
            this.id = id;
            this.owner = owner;
            this.nickname = nickname;
            this.email = email;
            this.avatar = avatar;
            this.bio = bio;
            this.title = title;
        }
    }
}

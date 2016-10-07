package prelich.curiomobile;

import android.app.Activity;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import prelich.curiomobile.dummy.ProjectContent;

public class ItemDetailFragment extends Fragment {

    //  The fragment argument representing the item ID that this fragment represents.
    public static final String ARG_ITEM_ID = "item_id";
    // The dummy content this fragment is presenting.
    private ProjectContent.Project mItem;
    // Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = ProjectContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.name);
                appBarLayout.setBackground(mItem.avatar);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        // Set header background
        Activity activity = this.getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mItem.name);
            appBarLayout.setBackground(mItem.avatar);
        }

        if (mItem != null) {

            // This following code sets up and populates the tabs for the project
            TabHost tabHost = (TabHost) rootView.findViewById(R.id.tab_host);
            tabHost.setup();
            final TabWidget tabWidget = tabHost.getTabWidget();
            final FrameLayout tabContent = tabHost.getTabContentView();

            // Get the original tab textviews and remove them from the viewgroup.
            TextView[] originalTextViews = new TextView[tabWidget.getTabCount()];
            for (int index = 0; index < tabWidget.getTabCount(); index++) {
                originalTextViews[index] = (TextView) tabWidget.getChildTabViewAt(index);
            }
            tabWidget.removeAllViews();

            // Ensure that all tab content childs are not visible at startup.
            for (int index = 0; index < tabContent.getChildCount(); index++) {
                tabContent.getChildAt(index).setVisibility(View.GONE);
            }

            /////////////////////////////
            // About Tab
            /////////////////////////////
            TabHost.TabSpec tabSpec1 = tabHost.newTabSpec((String) originalTextViews[0].getTag());
            final View aboutContentView = inflater.inflate(R.layout.tab1, container, false);

            if(!mItem.description.isEmpty())
                ((TextView) aboutContentView.findViewById(R.id.about_detail)).setText(mItem.description);
            else if (!mItem.short_description.isEmpty())
                ((TextView) aboutContentView.findViewById(R.id.about_detail)).setText(mItem.short_description);
            else
                ((TextView) aboutContentView.findViewById(R.id.about_detail)).setText("No description.");

            tabSpec1.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return aboutContentView;
                }
            });
            tabSpec1.setIndicator("About");
            tabHost.addTab(tabSpec1);

            /////////////////////////////
            // Research Questions Tab
            /////////////////////////////
            TabHost.TabSpec tabSpec2 = tabHost.newTabSpec((String) originalTextViews[1].getTag());
            final View questionsContentView = inflater.inflate(R.layout.tab2, container, false);
            ViewGroup questions = (ViewGroup) questionsContentView.findViewById(R.id.questions_holder);
            if(mItem.questions.size() > 0) {
                for(int i = 0; i < mItem.questions.size(); i++) {
                    TextView question_detail = new TextView(activity);
                    question_detail.setTextColor(Color.GRAY);
                    question_detail.setTextSize(18);
                    question_detail.setPadding(60,60,60,60);

                    ProjectContent.ResearchQuestion question = mItem.questions.get(i);
                    StringBuilder builder = new StringBuilder();
                    if(!question.title.isEmpty())
                        builder.append("Title: " + question.title);
                    if(!question.title.isEmpty())
                        builder.append("\nQuestion: " + question.question);
                    if(!question.motivation.isEmpty())
                        builder.append("\nMotivation: " + question.motivation);

                    question_detail.setText(builder.toString());
                    questions.addView(question_detail);
                }
            }
            else {
                TextView question_detail = new TextView(activity);
                question_detail.setTextColor(Color.GRAY);
                question_detail.setTextSize(18);
                question_detail.setPadding(60,0,0,0);
                question_detail.setText("No current questions.");;
                questions.addView(question_detail);
            }

            tabSpec2.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return questionsContentView;
                }
            });
            tabSpec2.setIndicator("Contribute");
            tabHost.addTab(tabSpec2);

            /////////////////////////////
            // Team Tab
            /////////////////////////////
            TabHost.TabSpec tabSpec3 = tabHost.newTabSpec((String) originalTextViews[2].getTag());
            final View teamContentView = inflater.inflate(R.layout.tab3, container, false);
            ViewGroup teamMembers = (ViewGroup) teamContentView.findViewById(R.id.team_holder);
            if(mItem.members.size() > 0) {
                for(int i = 0; i < mItem.members.size(); i++) {
                    LinearLayout team_member = new LinearLayout(activity);
                    team_member.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    team_member.setLayoutParams(LLParams);

                    TextView team_detail = new TextView(activity);
                    team_detail.setTextColor(Color.GRAY);
                    team_detail.setTextSize(18);
                    team_detail.setPadding(60,60,60,60);

                    ImageView team_avatar = new ImageView(activity);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(400,400);
                    team_avatar.setLayoutParams(layoutParams);
                    team_avatar.setPadding(60,60,60,60);

                    ProjectContent.Member member = mItem.members.get(i);
                    StringBuilder builder = new StringBuilder();
                    if(!member.owner.isEmpty())
                        builder.append("Owner: "+ member.owner);
                    if(!member.nickname.isEmpty())
                        builder.append("\nName: " + member.nickname);
                    if(!member.email.isEmpty())
                        builder.append("\nEmail: " + member.email);
                    if(!member.bio.isEmpty())
                        builder.append("\nBio: " + member.bio);
                    if(!member.title.isEmpty())
                        builder.append("\nTitle: " + member.title);

                    team_detail.setText(builder.toString());
                    team_avatar.setImageDrawable(member.avatar);

                    team_member.addView(team_avatar);
                    team_member.addView(team_detail);
                    teamMembers.addView(team_member);
                }
            }
            else {
                TextView team_detail = new TextView(activity);
                team_detail.setTextColor(Color.GRAY);
                team_detail.setTextSize(18);
                team_detail.setPadding(60,0,0,0);
                team_detail.setText("No current members.");;
                teamMembers.addView(team_detail);
            }

            tabSpec3.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return teamContentView;
                }
            });
            tabSpec3.setIndicator("Team");
            tabHost.addTab(tabSpec3);
        }

        return rootView;
    }
}

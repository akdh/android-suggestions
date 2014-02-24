1. Installation

Download the ADT bundle (with Eclipse) here: http://developer.android.com/sdk/index.html
Setup instructions here: http://developer.android.com/sdk/installing/bundle.html

--------------------------------------------------------------------------------

2. Apps

There are 3 android apps: 
    - Survey: list view; clicking on a title opens a new page with the description
    - Survey2: expandable list view; clicking on a title expands the description
        - http://developer.android.com/reference/android/widget/ExpandableListView.html
    - Survey3: swipe between description pages
        - http://developer.android.com/training/implementing-navigation/lateral.html

2.1 Survey
    Same general idea as the other two :)

2.2 Survey2
    DisplayContext.java (displays a TextView telling user which context city they're looking at).
        INTENT: recieves contextIndex from ExpandableListAdapter.java; default 1
        INTENT: passes context id number to SuggestionList.java

    SuggestionList.java: ExpandableListView of the attractions from the context recieved in the intent.
        - clicking on a title expands the description, a link to the website, and a button to select/
            bookmark the attraction
        - clicking an attraction while a different one is open closes the first attraction
        - when fewer than two attractions are selected/bookmarked, a Toast appears to acknowledge it was
            bookmarked successfully
        - when two attractions are selected/bookmarked, an AlertDialog appears with options to 
            go to a new city or continue
            - when all of the attractions in a context are selected/bookmarked, the only option is 
                to go to a new city
            - if the user selects to go to a new city, they return to DisplayContext.java
            - on the last context, the options in the AlertDialog are "Finish" and "Continue"
            
    Finish.java: TODO (currently displays Hello World)
    
    HttpRequest.java: makes GET/POST requests to the PHP scripts on PLG
    
    ExpandableListAdapter.java: adapter for the ExpandableListView; contains click listeners for the 
        website/select buttons, etc.
        INTENT: passes next contextIndex to DisplayContext.java when 'Select new city' button clicked
        
2.3 Survey 3
    - swipe navigation between different attractions
    
    DisplayContext.java (displays a TextView telling user which context city they're looking at).
        INTENT: recieves contextIndex from ExpandableListAdapter.java; default 1
        INTENT: passes context id number to Suggestion.java
        
    Suggestion.java: Gets attraction data from server and loads it into the FragmentStatePagerAdapter.
    
    mFragment.java: OnClickListeners for web/select buttons and AlertDialog buttons
        - when fewer than two attractions are selected/bookmarked, a Toast appears to acknowledge it was
            bookmarked successfully
        - when two attractions are selected/bookmarked, an AlertDialog appears with options to 
            go to a new city or continue
            - when all of the attractions in a context are selected/bookmarked, the only option is 
                to go to a new city
            - if the user selects to go to a new city, they return to DisplayContext.java
            - on the last context, the options in the AlertDialog are "Finish" and "Continue"
            
    Finish.java: TODO (currently displays Hello World)
    
    HttpRequest.java: makes GET/POST requests to the PHP scripts on PLG
    
    PageAdapter.java
    
    PageChangeListener.java: calls HttpRequest when a user swipes to a new page
    
--------------------------------------------------------------------------------

3. Server Requests

3.1 Survey

- contexts1.php
    See contexts2.php.
    
- list1.php
    See list2.php.
    
- post1.php
    Parameters:
        "name": eg. suggestion_select_RUNID_PROFILE_CONTEXT_RANK
        "value" (optional): 1
        "duration" (optional): length of time in milliseconds the attraction was viewed for
    Returns nothing.

3.2 Survey2

- contexts2.php
    Called from DisplayContext.java. 
    Takes no parameters. 
    Returns JSON of format:
    {
        "success":1, 
        "contexts": {
            "1": {
                "context_city":"CITY,STATE",
                "context":"CONTEXT_NUM"
            },
            ...
        }
    }
    If there is an error connecting to the database, returns "success":0.
    
- list2.php
    Called from LoadSuggestionList class in SuggestionList.java. 
    Parameters:
        "context": context id number
    Returns JSON of format:
    {
        "success":1, 
        "attractions": {
            "1": {
                "runid_num":"RUNID_NUM",
                "profile":"PROFILE",
                "context":"CONTEXT",
                "rank":"RANK",
                "title":"TITLE",
                "description":"DESCRIPTION",
                "url":"URL"
            },
            ...
        }
    }
    If there is an error connecting to the database, returns "success":0.

- post2.php
    Called from postData class in SuggestionList.java.
    Parameters:
        "name": eg. suggestion_select_RUNID_PROFILE_CONTEXT_RANK
        "value": 1
        "name2" (optional): suggestion_close_RUNID_PROFILE_CONTEXT_RANK for the attraction that 
            closes automatically when a different one is expanded
        "value2" (optional): 1
        "time" (optional): length of time in milliseconds the attraction was viewed for
    Returns nothing.

3.3 Survey3

- contexts3.php
    See contexts2.php.

- list3.php
    Called from LoadSuggestionList class in Suggestion.java. 
    See list2.php.
    
- post3.php
    Called from postData class in Suggestion.java.
    Parameters:
        "name": eg. suggestion_select_RUNID_PROFILE_CONTEXT_RANK
        "value": 1
        "name2" (optional): "name" field of the last viewed suggestion, used for updating the duration
        "duration" (optional): length of time in milliseconds the attraction was viewed for
    Returns nothing.
            

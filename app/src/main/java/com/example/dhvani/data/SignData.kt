package com.example.dhvani.data

import com.example.dhvani.data.model.SignCategory

object SignData {
    val categoryMapping = mapOf(
        SignCategory.BASICS to listOf(
            "Hello", "Thank You", "Please", "Yes", "No", "Welcome", "Good", "Bad", "Sorry", "Help",
            "Friend", "Word", "Message", "Sign", "Nice", "Together", "Question", "Answer", "Idea",
            "Information", "Topic", "Language", "Expression", "Request", "Secret", "Project", "Skill",
            "Meaning", "Focus", "Confirm", "Both", "Whole", "Address", "Name", "Very", "Alright"
        ),
        SignCategory.NUMBER to listOf(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "30", "40", "50", "60", "70", "80", "90", "100", "1000", "Zero"
        ),
        SignCategory.FAMILY to listOf(
            "Mother", "Father", "Brother", "Sister", "Son", "Daughter", "Grandfather", "Grandmother",
            "Grandson", "Granddaughter", "Parents", "Wife", "Husband", "Twins", "Relative", "Neighbor",
            "Guest", "People", "Person", "Family", "Stepbrother", "Stepsister", "Stepmother", "Stepfather",
            "Brother-In-Law", "Sister-In-Law", "Mother-In-Law", "Father-In-Law", "Baby", "Birth", "Boy",
            "Children", "Enemy", "Engagement", "Girl", "Paternal Aunt", "Paternal Uncle", "Society",
            "Stepdaughter", "Stepson", "Wedding", "Woman", "Man", "Divorce", "Surname"
        ),
        SignCategory.EMOTIONS to listOf(
            "Happy", "Sad", "Angry", "Fear", "Love", "Hate", "Jealous", "Proud", "Excited", "Nervous",
            "Confident", "Shy", "Timid", "Upset", "Funny", "Kind", "Cruel", "Greedy", "Wise", "Smart",
            "Lazy", "Brave", "Strong", "Weak", "Calm", "Soft", "Rude", "Polite", "Serious", "Clever",
            "Responsible", "Careful", "Careless", "Energetic", "Humorous", "Interesting", "Boring",
            "Satisfied", "Selfish", "Generous", "Stubborn", "Attention", "Behavior", "Confidence",
            "Hope", "Patience", "Properly", "Trouble", "Trust", "Wrong", "Disappointed", "Irritable",
            "Cunning", "Foolish", "Frank"
        ),
        SignCategory.BODY to listOf(
            "Head", "Hand", "Finger", "Eye", "Ear", "Nose", "Mouth", "Tongue", "Teeth", "Neck",
            "Chest", "Heart", "Brain", "Blood", "Bone", "Skin", "Hair", "Leg", "Feet", "Knees",
            "Shoulder", "Waist", "Hip", "Throat", "Stomach", "Pain", "Health", "Arm", "Back",
            "Face", "Lips", "Thigh", "Toes", "Voice", "Sweat", "Elbow"
        ),
        SignCategory.HOME to listOf(
            "House", "Room", "Kitchen", "Bathroom", "Bedroom", "Balcony", "Door", "Window", "Gate",
            "Roof", "Terrace", "Table", "Chair", "Sofa", "Bed", "Bedsheet", "Cupboard", "Shelf",
            "Drawer", "Mirror", "Clock", "Fan", "Bulb", "Light", "Fridge", "Bottle", "Bucket",
            "Bowl", "Plate", "Cup", "Mug", "Knife", "Spoon", "Pan", "Gas Stove", "Mixer", "Soap",
            "Towel", "Toilet", "Washbasin", "Toothbrush", "Toothpaste", "Umbrella", "Key", "Lock",
            "Curtain", "Mat", "Torch", "Watch", "Newspaper", "Box", "Candle", "Comb", "Matchbox",
            "Pump", "Room Heater", "Stove", "Strainer", "Tubelight", "Vessel", "Pillow", "Cushion",
            "Rubber", "Eraser", "Tongs", "Wall", "Zip", "Purse"
        ),
        SignCategory.FOOD to listOf(
            "Water", "Tea", "Coffee", "Milk", "Juice", "Cold Drink", "Breakfast", "Lunch", "Dinner",
            "Meal", "Rice", "Chapati", "Bread", "Butter", "Cheese", "Chocolate", "Cake", "Ice Cream",
            "Chicken", "Meat", "Vegetarian", "Non-Vegetarian", "Fruit", "Vegetable", "Potato", "Tomato",
            "Onion", "Carrot", "Brinjal", "Pumpkin", "Mango", "Banana", "Apple", "Orange", "Papaya",
            "Grapes", "Pineapple", "Watermelon", "Sugar", "Salt", "Pepper", "Oil", "Spice", "Pickle",
            "Sweets", "Dosa", "Idli", "Samosa", "Puri", "Wada", "Lassi", "Sharbat", "Dal", "Curry",
            "Popcorn", "Biscuit", "Ghee", "Jam", "Laddu", "Masala", "Sandwich", "Toast", "Chickoo",
            "Coconut", "Custard Apple", "Guava", "Jackfruit", "Pomegranate", "Sugarcane", "Sweet Lime",
            "Beans", "Cabbage", "Cauliflower", "Chilli", "Cucumber", "Drum Stick", "Garlic", "Ginger",
            "Lady's Finger", "Leafy Vegetable", "Lemon", "Pea", "Radish", "White Gourd", "Corn",
            "Grains", "Groundnut", "Jowar", "Pulses", "Wheat", "Cashew", "Almonds", "Mushroom", "Paan",
            "Sauce", "Diet", "Flour"
        ),
        SignCategory.ANIMALS to listOf(
            "Dog", "Cat", "Cow", "Lion", "Tiger", "Monkey", "Fish", "Bird", "Snake", "Spider",
            "Butterfly", "Mosquito", "Crow", "Horse", "Goat", "Buffalo", "Rat", "Pig", "Hen",
            "Peacock", "Squirrel", "Tree", "Flower", "Leaf", "Seed", "Grass", "Rose", "Lotus",
            "Sunflower", "Garden", "River", "Lake", "Sea", "Waterfall", "Mountain", "Hill",
            "Island", "Forest", "Jungle", "Stone", "Sand", "Mud", "Earth", "Sky", "Sun", "Moon",
            "Star", "Cloud", "Rain", "Thunder", "Lightning", "Storm", "Wind", "Snow", "Ice",
            "Climate", "Weather", "Summer", "Winter", "Deer", "Tail", "Egg", "Feather", "Frog",
            "Nest", "Pigeon", "Sheep", "Ant", "Bug", "Cockroach", "Hole", "Housefly", "Reptile",
            "Branch", "Bud", "Creeper", "Plant", "Thorn", "Crocodile", "Wings"
        ),
        SignCategory.TIME to listOf(
            "Time", "Today", "Tomorrow", "Yesterday", "Morning", "Afternoon", "Evening", "Night",
            "Noon", "Second", "Minute", "Hour", "Day", "Week", "Month", "Year", "Sunday", "Monday",
            "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "January", "February", "March",
            "April", "May", "June", "July", "August", "September", "October", "November", "December",
            "Holiday", "New Year", "Birthday", "Date", "Age", "Early", "Last", "Late", "Later",
            "Never", "Next", "Often", "Until", "Day After", "Day Before", "Now", "Weekday"
        ),
        SignCategory.EDUCATION to listOf(
            "School", "Student", "Teacher", "Principal", "Class", "Classroom", "Book", "Notebook",
            "Pen", "Pencil", "Blackboard", "Chalk", "Desk", "Bench", "Bag", "School Bus", "Exam",
            "Lesson", "Chapter", "Study", "Learn", "Read", "Write", "Education", "College",
            "University", "Laboratory", "Library", "Science", "Mathematics", "Biology", "Chemistry",
            "Physics", "History", "Geography", "Essay", "Poetry", "Sentence", "Story", "Knowledge",
            "Experiment", "Dictionary", "Projector", "Certificate", "Explain", "Imagine", "Understand",
            "Uniform", "Duster", "File", "Napkin", "Page", "Paper", "Picture", "Ruler", "Sharpener",
            "Tiffin Box", "Water Bottle", "Add", "Divide", "Multiply", "Subtract", "Civics",
            "Economics", "Equator", "Geometry", "Radius", "Vapor", "Map"
        ),
        SignCategory.TECHNOLOGY to listOf(
            "Mobile", "Smart Phone", "Laptop", "Computer", "Keyboard", "Mouse", "Monitor", "Scanner",
            "Printer", "Website", "Google", "Youtube", "Facebook", "Whatsapp", "Twitter", "Wifi",
            "Internet", "Network", "App", "Online Meeting", "Video Call", "Zoom Meeting", "Email",
            "Password", "Recharge", "Charger", "Video", "Camera", "Hearing Aid", "Television",
            "Radio", "Technology", "C.D.", "Chart", "Model", "O.H.P.", "Poster", "Record", "Shoot",
            "Show", "A.C.", "Aadhar Card", "A.T.M. Card", "P.A.N. Card", "Voter Card", "Headphone",
            "Speakers"
        ),
        SignCategory.TRANSPORT to listOf(
            "Bus", "Car", "Taxi", "Scooter", "Bike", "Van", "Train", "Railway", "Ticket",
            "Train Station", "Airport", "Aeroplane", "Helicopter", "Boat", "Ship", "Road",
            "Highway", "Bridge", "Traffic", "Traffic Light", "Signal", "Footpath", "Crossroads",
            "Platform", "Reservation", "Seat", "Berth", "Track", "Transport", "Route", "Travel",
            "Autorickshaw", "Cycle", "Jeep", "Zebra Crossing", "Bogie", "Engine", "Guard",
            "Lighthouse", "Pass", "Rocket", "T. C.", "Waterway", "Flyover"
        ),
        SignCategory.WORK to listOf(
            "Doctor", "Engineer", "Lawyer", "Police", "Soldier", "Pilot", "Farmer", "Fisherman",
            "Driver", "Mechanic", "Electrician", "Plumber", "Carpenter", "Tailor", "Welder",
            "Potter", "Gardener", "Milkman", "Postman", "Nurse", "Officer", "Manager", "Director",
            "Writer", "Editor", "Actor", "Actress", "Business", "Job", "Office", "Salary",
            "Service", "Profession", "Axe", "Bolt", "Brick", "Button", "Cement", "Cobbler",
            "Draughtsman", "Hammer", "Ladder", "Machine", "Motor", "Nail", "Nut", "Paint",
            "Pipe", "Plug", "Rope", "Saw", "Scissors", "Spanner", "Switch", "Tools", "Weaver",
            "Wheel", "Wire", "Barber", "Bus Conductor", "Calculator", "Chemist", "Clerk",
            "Coolie", "Dhobi", "Peon", "Surgeon", "Typist", "Boss", "Waiter", "Staff"
        ),
        SignCategory.ACTIONS to listOf(
            "Eat", "Drink", "Walk", "Run", "Jump", "Sleep", "Talk", "Speak", "Listen", "Hear",
            "Watch", "See", "Think", "Read", "Write", "Study", "Learn", "Teach", "Cook", "Wash",
            "Clean", "Open", "Close", "Push", "Pull", "Give", "Take", "Come", "Go", "Sit",
            "Stand", "Work", "Play", "Travel", "Drive", "Ride", "Buy", "Sell", "Help", "Support",
            "Search", "Find", "Try", "Practice", "Use", "Wear", "Wait", "Smile", "Laugh", "Cry",
            "Fight", "Protect", "Save", "Share", "Throw", "Catch", "Swim", "Climb", "Build",
            "Repair", "Move", "Turn", "Call", "Meet", "Invite", "Join", "Begin", "Finish",
            "Absorb", "Accept", "Accuse", "Advise", "Agree", "Allow", "Announce", "Appear",
            "Arrange", "Arrest", "Bake", "Beat", "Become", "Bend", "Blow", "Boil", "Borrow",
            "Break", "Brush", "Carry", "Change", "Chase", "Chat", "Clap", "Co-Operate",
            "Collect", "Communicate", "Compare", "Confuse", "Continue", "Control", "Cover",
            "Criticize", "Cross", "Cut", "Decide", "Decrease", "Defend", "Depend", "Destroy",
            "Develop", "Dig", "Disagree", "Disappear", "Discover", "Disguise", "Dislike",
            "Distribute", "Do", "Doubt", "Dream", "Drop", "Drown", "Earn", "Encourage",
            "Escape", "Examine", "Experience", "Fail", "Fall", "Feed", "Feel", "Fill",
            "Float", "Follow", "Force", "Freeze", "Fry", "Get", "Gossip", "Grind", "Guess",
            "Hang", "Happen", "Harvest", "Hide", "Hold", "Hop", "Hurt", "Ignore", "Improve",
            "Inaugurate", "Interfere", "Introduce", "Invent", "Keep", "Kick", "Kill", "Kiss",
            "Knit", "Knock", "Leak", "Leave", "Lend", "Lick", "Like", "Make", "Miss", "Mix",
            "Order", "Pay", "Pick", "Plan", "Plow", "Postpone", "Pour", "Praise", "Prepare",
            "Press", "Pretend", "Program", "Promise", "Prove", "Publish", "Put", "Reach",
            "Recognize", "Regret", "Remove", "Resign", "Respect", "Rest", "Return", "Roam",
            "Roll", "Separate", "Shake", "Shave", "Shout", "Sink", "Skip", "Slap", "Slip",
            "Solve", "Spill", "Spoil", "Squeeze", "Stay", "Stick", "Stir", "Stitch", "Stop",
            "Store", "Supply", "Suspect", "Swallow", "Swing", "Take Revenge", "Tear", "Tease",
            "Touch", "Visit", "Vomit", "Wake-Up", "Want", "Warn", "Waste", "Wipe", "Wish",
            "Worry", "Tie", "Dye", "Yawn", "Blink", "Arrive", "Connect", "Report", "Memorise",
            "Reply", "Say", "Tell"
        ),
        SignCategory.COLORS to listOf(
            "Red", "Blue", "Green", "Yellow", "Orange", "Purple", "Pink", "Brown", "Black", "White",
            "Grey", "Silver", "Gold", "Violet", "Indigo", "Dark", "Light", "Color", "Bright"
        ),
        SignCategory.GRAMMAR to listOf(
            "Adjective", "Basic Hand Position", "Conjunction", "Continuous Tense", "Future Tense",
            "Grammar", "Idiom", "Noun", "Past Tense", "Perfect Tense", "Plural", "Preposition",
            "Present Tense", "Pronoun", "Proverb", "Singular", "Tense", "Verb"
        ),
        SignCategory.RELIGION to listOf(
            "Allah", "Angel", "Bell", "Bible", "Buddha", "Buddhist", "Christian", "Church", "Devil",
            "Durga", "Ghost", "Gita", "Hanuman", "Hindu", "Jain", "Jesus", "Jew", "Mary", "Muslim",
            "Parsi", "Pray", "Priest", "Ram", "Religion", "Shiva", "Sikh", "Soul", "Temple"
        ),
        SignCategory.SPORTS to listOf(
            "Badminton", "Ball", "Basketball", "Boxing", "Caram", "Century", "Chess", "Cricket",
            "Football", "Game", "Goal", "Hockey", "Kabaddi", "Karate", "Kite", "Lose", "Olympics",
            "Race", "Sports", "Table Tennis", "Tennis", "Trophy", "Volleyball", "Win", "Wrestling"
        ),
        SignCategory.MISC to listOf(
            "Assembly", "Bundh", "Capital", "Crown", "Duty", "Election", "Flag", "Government",
            "King", "Leader", "Minister", "Parliament", "Party", "Power", "Queen", "Riot",
            "Strike", "Vote", "Crime", "Justice", "Oath", "Punish", "Rule"
        )
    )

    val signsByAlphabets = ('A'..'Z').map { it.toString() }
}

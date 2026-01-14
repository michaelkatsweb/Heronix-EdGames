package com.heronix.edu.server.service;

import com.heronix.edu.server.entity.QuestionEntity;
import com.heronix.edu.server.entity.QuestionSetEntity;
import com.heronix.edu.server.repository.QuestionRepository;
import com.heronix.edu.server.repository.QuestionSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Loads preset question sets into the database on application startup.
 * These are public question sets available to all teachers.
 */
@Component
public class PresetQuestionDataLoader implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(PresetQuestionDataLoader.class);

    private final QuestionSetRepository questionSetRepository;
    private final QuestionRepository questionRepository;

    public PresetQuestionDataLoader(QuestionSetRepository questionSetRepository,
                                     QuestionRepository questionRepository) {
        this.questionSetRepository = questionSetRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Check if presets already loaded
        if (questionSetRepository.findByIsPublicTrueOrderByNameAsc().size() > 0) {
            logger.info("Preset question sets already loaded");
            return;
        }

        logger.info("Loading preset question sets...");

        // Math - Elementary (K-2)
        createMathK2Preset();

        // Math - Elementary (3-5)
        createMath35Preset();

        // Math - Middle School (6-8)
        createMath68Preset();

        // Math - High School (9-12)
        createMath912Preset();

        // Science - Elementary (3-5)
        createScience35Preset();

        // Science - Middle School (6-8)
        createScience68Preset();

        // History - US History
        createUSHistoryPreset();

        // Civics & Government
        createCivicsPreset();

        // Geography
        createGeographyPreset();

        // English/Language Arts
        createEnglishPreset();

        logger.info("Preset question sets loaded successfully");
    }

    private void createMathK2Preset() {
        QuestionSetEntity set = createSet("math-k2-basics", "Math Basics (K-2)",
            "Basic addition, subtraction, and number recognition for early learners",
            "Mathematics", "K-2");

        addQuestion(set, 1, "What is 2 + 3?", "5", "4", "6", "7");
        addQuestion(set, 2, "What is 5 - 2?", "3", "2", "4", "5");
        addQuestion(set, 3, "What is 1 + 1?", "2", "1", "3", "0");
        addQuestion(set, 4, "What is 4 + 4?", "8", "6", "7", "9");
        addQuestion(set, 5, "What is 6 - 3?", "3", "2", "4", "5");
        addQuestion(set, 6, "What is 7 + 2?", "9", "8", "10", "7");
        addQuestion(set, 7, "What is 10 - 5?", "5", "4", "6", "3");
        addQuestion(set, 8, "What is 3 + 5?", "8", "7", "9", "6");
        addQuestion(set, 9, "What number comes after 9?", "10", "8", "11", "7");
        addQuestion(set, 10, "What is 8 - 4?", "4", "3", "5", "6");
        addQuestion(set, 11, "Which is bigger: 5 or 3?", "5", "3", "They're equal", "Neither");
        addQuestion(set, 12, "What is 0 + 5?", "5", "0", "6", "4");
        addQuestion(set, 13, "What is 9 - 9?", "0", "1", "9", "18");
        addQuestion(set, 14, "What is 2 + 2 + 2?", "6", "4", "8", "5");
        addQuestion(set, 15, "How many sides does a triangle have?", "3", "4", "2", "5");

        questionSetRepository.save(set);
    }

    private void createMath35Preset() {
        QuestionSetEntity set = createSet("math-35-operations", "Math Operations (3-5)",
            "Multiplication, division, and fractions for upper elementary",
            "Mathematics", "3-5");

        addQuestion(set, 1, "What is 6 × 7?", "42", "36", "48", "35");
        addQuestion(set, 2, "What is 56 ÷ 8?", "7", "6", "8", "9");
        addQuestion(set, 3, "What is 1/2 + 1/2?", "1", "2", "1/4", "2/4");
        addQuestion(set, 4, "What is 9 × 9?", "81", "72", "90", "63");
        addQuestion(set, 5, "What is 144 ÷ 12?", "12", "11", "13", "14");
        addQuestion(set, 6, "What is 3/4 of 20?", "15", "12", "16", "10");
        addQuestion(set, 7, "What is 8 × 12?", "96", "84", "108", "92");
        addQuestion(set, 8, "What is the perimeter of a square with sides of 5?", "20", "25", "15", "10");
        addQuestion(set, 9, "What is 100 - 37?", "63", "67", "73", "57");
        addQuestion(set, 10, "What is 2/5 as a decimal?", "0.4", "0.2", "0.5", "0.25");
        addQuestion(set, 11, "What is 15 × 4?", "60", "55", "65", "50");
        addQuestion(set, 12, "What is 81 ÷ 9?", "9", "8", "7", "10");
        addQuestion(set, 13, "What is 1/3 + 2/3?", "1", "3/3", "2/3", "1/3");
        addQuestion(set, 14, "What is the area of a rectangle 6 × 8?", "48", "28", "42", "54");
        addQuestion(set, 15, "What is 250 + 175?", "425", "415", "435", "325");

        questionSetRepository.save(set);
    }

    private void createMath68Preset() {
        QuestionSetEntity set = createSet("math-68-prealgebra", "Pre-Algebra (6-8)",
            "Integers, ratios, percentages, and basic algebra",
            "Mathematics", "6-8");

        addQuestion(set, 1, "What is -5 + 8?", "3", "-3", "13", "-13");
        addQuestion(set, 2, "What is 15% of 200?", "30", "25", "35", "20");
        addQuestion(set, 3, "Solve for x: 2x = 14", "7", "12", "16", "28");
        addQuestion(set, 4, "What is the ratio 12:16 in simplest form?", "3:4", "4:3", "6:8", "2:3");
        addQuestion(set, 5, "What is (-3) × (-4)?", "12", "-12", "-7", "7");
        addQuestion(set, 6, "What is 3² + 4²?", "25", "14", "49", "7");
        addQuestion(set, 7, "Solve: 3x + 5 = 20", "5", "15", "25", "7");
        addQuestion(set, 8, "What is 40% as a fraction?", "2/5", "4/10", "1/4", "2/4");
        addQuestion(set, 9, "What is the value of √81?", "9", "8", "7", "81");
        addQuestion(set, 10, "What is -12 ÷ 4?", "-3", "3", "-8", "8");
        addQuestion(set, 11, "If y = 2x + 3, what is y when x = 4?", "11", "9", "14", "10");
        addQuestion(set, 12, "What is 0.75 as a fraction?", "3/4", "7/5", "1/4", "3/5");
        addQuestion(set, 13, "What is the mean of 4, 6, 8, 10, 12?", "8", "6", "10", "7");
        addQuestion(set, 14, "Solve: x/4 = 12", "48", "3", "16", "8");
        addQuestion(set, 15, "What is 5³?", "125", "15", "25", "75");

        questionSetRepository.save(set);
    }

    private void createMath912Preset() {
        QuestionSetEntity set = createSet("math-912-algebra", "Algebra & Geometry (9-12)",
            "Algebraic equations, quadratics, and geometry concepts",
            "Mathematics", "9-12");

        addQuestion(set, 1, "Solve: x² - 9 = 0", "x = ±3", "x = 3", "x = 9", "x = ±9");
        addQuestion(set, 2, "What is the slope of y = 3x + 7?", "3", "7", "3x", "10");
        addQuestion(set, 3, "Factor: x² + 5x + 6", "(x+2)(x+3)", "(x+1)(x+6)", "(x+2)(x+4)", "(x+3)(x+3)");
        addQuestion(set, 4, "What is sin(90°)?", "1", "0", "-1", "undefined");
        addQuestion(set, 5, "Solve: 2x² = 32", "x = ±4", "x = 4", "x = 16", "x = ±16");
        addQuestion(set, 6, "What is the distance formula?", "√((x₂-x₁)² + (y₂-y₁)²)", "(x₂-x₁) + (y₂-y₁)", "2πr", "bh/2");
        addQuestion(set, 7, "What is log₁₀(1000)?", "3", "10", "100", "1000");
        addQuestion(set, 8, "If f(x) = x² + 1, what is f(3)?", "10", "9", "7", "4");
        addQuestion(set, 9, "What is the quadratic formula?", "(-b ± √(b²-4ac))/2a", "ax² + bx + c", "b² - 4ac", "-b/2a");
        addQuestion(set, 10, "What is cos(0°)?", "1", "0", "-1", "undefined");
        addQuestion(set, 11, "Simplify: (x³)(x²)", "x⁵", "x⁶", "x¹", "2x⁵");
        addQuestion(set, 12, "What is the area of a circle with radius 5?", "25π", "10π", "5π", "50π");
        addQuestion(set, 13, "Solve: |x - 3| = 7", "x = 10 or x = -4", "x = 10", "x = -4", "x = 4");
        addQuestion(set, 14, "What is the y-intercept of y = 2x - 5?", "-5", "2", "5", "-2");
        addQuestion(set, 15, "What is 8^(2/3)?", "4", "8", "2", "16");

        questionSetRepository.save(set);
    }

    private void createScience35Preset() {
        QuestionSetEntity set = createSet("science-35-general", "General Science (3-5)",
            "Basic science concepts for elementary students",
            "Science", "3-5");

        addQuestion(set, 1, "What planet is closest to the Sun?", "Mercury", "Venus", "Earth", "Mars");
        addQuestion(set, 2, "What is the largest organ in the human body?", "Skin", "Heart", "Brain", "Liver");
        addQuestion(set, 3, "What is H₂O commonly known as?", "Water", "Oxygen", "Hydrogen", "Salt");
        addQuestion(set, 4, "How many legs does a spider have?", "8", "6", "10", "4");
        addQuestion(set, 5, "What force keeps us on the ground?", "Gravity", "Magnetism", "Friction", "Electricity");
        addQuestion(set, 6, "What is the boiling point of water in Celsius?", "100°C", "0°C", "50°C", "212°C");
        addQuestion(set, 7, "Which planet is known as the Red Planet?", "Mars", "Jupiter", "Venus", "Saturn");
        addQuestion(set, 8, "What do plants need to make food?", "Sunlight", "Darkness", "Salt", "Wind");
        addQuestion(set, 9, "What is the hardest natural substance?", "Diamond", "Gold", "Iron", "Marble");
        addQuestion(set, 10, "How many bones are in the adult human body?", "206", "205", "300", "150");
        addQuestion(set, 11, "What gas do plants release?", "Oxygen", "Carbon dioxide", "Nitrogen", "Hydrogen");
        addQuestion(set, 12, "What is the center of an atom called?", "Nucleus", "Electron", "Proton", "Neutron");
        addQuestion(set, 13, "What type of animal is a frog?", "Amphibian", "Reptile", "Mammal", "Fish");
        addQuestion(set, 14, "What causes day and night?", "Earth's rotation", "Moon's movement", "Sun's movement", "Clouds");
        addQuestion(set, 15, "What is the process plants use to make food?", "Photosynthesis", "Respiration", "Digestion", "Fermentation");

        questionSetRepository.save(set);
    }

    private void createScience68Preset() {
        QuestionSetEntity set = createSet("science-68-life", "Life Science (6-8)",
            "Biology and life science for middle school",
            "Science", "6-8");

        addQuestion(set, 1, "What is the powerhouse of the cell?", "Mitochondria", "Nucleus", "Ribosome", "Cell membrane");
        addQuestion(set, 2, "What is DNA's shape called?", "Double helix", "Spiral", "Circle", "Triangle");
        addQuestion(set, 3, "Which organ pumps blood through the body?", "Heart", "Lungs", "Liver", "Brain");
        addQuestion(set, 4, "What is the basic unit of life?", "Cell", "Atom", "Molecule", "Organ");
        addQuestion(set, 5, "What carries genetic information?", "Chromosomes", "Ribosomes", "Lysosomes", "Vacuoles");
        addQuestion(set, 6, "What type of blood cells fight infection?", "White blood cells", "Red blood cells", "Platelets", "Plasma");
        addQuestion(set, 7, "What is the largest planet in our solar system?", "Jupiter", "Saturn", "Neptune", "Uranus");
        addQuestion(set, 8, "What is cellular respiration?", "Converting glucose to energy", "Making glucose from sunlight", "Dividing cells", "Removing waste");
        addQuestion(set, 9, "What part of the plant absorbs water?", "Roots", "Leaves", "Stem", "Flower");
        addQuestion(set, 10, "What is the study of heredity called?", "Genetics", "Ecology", "Anatomy", "Botany");
        addQuestion(set, 11, "What is the pH of pure water?", "7", "0", "14", "1");
        addQuestion(set, 12, "What are the building blocks of proteins?", "Amino acids", "Fatty acids", "Nucleotides", "Sugars");
        addQuestion(set, 13, "What system includes the brain and spinal cord?", "Nervous system", "Digestive system", "Circulatory system", "Skeletal system");
        addQuestion(set, 14, "What is the process of cell division called?", "Mitosis", "Meiosis", "Osmosis", "Diffusion");
        addQuestion(set, 15, "What is an organism that makes its own food?", "Producer", "Consumer", "Decomposer", "Predator");

        questionSetRepository.save(set);
    }

    private void createUSHistoryPreset() {
        QuestionSetEntity set = createSet("history-us-general", "US History",
            "Key events and figures in American history",
            "History", "6-8");

        addQuestion(set, 1, "In what year did the American Revolution begin?", "1775", "1776", "1774", "1783");
        addQuestion(set, 2, "Who was the first President of the United States?", "George Washington", "John Adams", "Thomas Jefferson", "Benjamin Franklin");
        addQuestion(set, 3, "What document declared independence from Britain?", "Declaration of Independence", "Constitution", "Bill of Rights", "Magna Carta");
        addQuestion(set, 4, "In what year did the Civil War end?", "1865", "1861", "1863", "1870");
        addQuestion(set, 5, "Who wrote the 'I Have a Dream' speech?", "Martin Luther King Jr.", "Malcolm X", "Rosa Parks", "John F. Kennedy");
        addQuestion(set, 6, "What was the name of the ship that brought Pilgrims to America?", "Mayflower", "Santa Maria", "Nina", "Pinta");
        addQuestion(set, 7, "Who was President during the Civil War?", "Abraham Lincoln", "Andrew Johnson", "Ulysses S. Grant", "George Washington");
        addQuestion(set, 8, "What amendment abolished slavery?", "13th Amendment", "14th Amendment", "15th Amendment", "1st Amendment");
        addQuestion(set, 9, "What was the Louisiana Purchase?", "Land bought from France", "Land bought from Spain", "Land bought from Britain", "Land bought from Mexico");
        addQuestion(set, 10, "In what year did World War II end?", "1945", "1944", "1946", "1943");
        addQuestion(set, 11, "Who was the President during the Great Depression?", "Franklin D. Roosevelt", "Herbert Hoover", "Harry Truman", "Calvin Coolidge");
        addQuestion(set, 12, "What event started World War I?", "Assassination of Archduke Franz Ferdinand", "Sinking of Lusitania", "Treaty of Versailles", "German invasion of Poland");
        addQuestion(set, 13, "What was the Boston Tea Party protesting?", "Taxation without representation", "British soldiers", "Tea prices", "Trade restrictions");
        addQuestion(set, 14, "Who invented the light bulb?", "Thomas Edison", "Benjamin Franklin", "Nikola Tesla", "Alexander Graham Bell");
        addQuestion(set, 15, "What was the Underground Railroad?", "A network to help enslaved people escape", "An actual railroad", "A coal mine system", "A subway system");

        questionSetRepository.save(set);
    }

    private void createCivicsPreset() {
        QuestionSetEntity set = createSet("civics-government", "Civics & Government",
            "US government structure and civic responsibilities",
            "Civics", "6-8");

        addQuestion(set, 1, "How many branches of government are there?", "3", "2", "4", "5");
        addQuestion(set, 2, "What is the legislative branch?", "Congress", "President", "Supreme Court", "Cabinet");
        addQuestion(set, 3, "How many senators does each state have?", "2", "1", "4", "Depends on population");
        addQuestion(set, 4, "What is the minimum age to be President?", "35", "30", "40", "25");
        addQuestion(set, 5, "How long is a presidential term?", "4 years", "2 years", "6 years", "8 years");
        addQuestion(set, 6, "What does the judicial branch do?", "Interprets laws", "Makes laws", "Enforces laws", "Vetoes laws");
        addQuestion(set, 7, "How many justices are on the Supreme Court?", "9", "7", "11", "12");
        addQuestion(set, 8, "What is the Bill of Rights?", "First 10 amendments", "The Constitution", "Declaration of Independence", "First 5 amendments");
        addQuestion(set, 9, "Who is the Commander in Chief of the military?", "The President", "Secretary of Defense", "Chairman of Joint Chiefs", "Vice President");
        addQuestion(set, 10, "What is the voting age in the US?", "18", "21", "16", "25");
        addQuestion(set, 11, "What does the First Amendment protect?", "Freedom of speech", "Right to bear arms", "Right to a fair trial", "Freedom from searches");
        addQuestion(set, 12, "Who has the power to declare war?", "Congress", "President", "Supreme Court", "Vice President");
        addQuestion(set, 13, "What is the supreme law of the land?", "The Constitution", "Bill of Rights", "Federal laws", "State laws");
        addQuestion(set, 14, "How many amendments are in the Constitution?", "27", "10", "25", "30");
        addQuestion(set, 15, "What is the purpose of the Electoral College?", "To elect the President", "To elect Senators", "To elect Representatives", "To elect Governors");

        questionSetRepository.save(set);
    }

    private void createGeographyPreset() {
        QuestionSetEntity set = createSet("geography-world", "World Geography",
            "Countries, capitals, and geographic features",
            "Geography", "6-8");

        addQuestion(set, 1, "What is the largest continent?", "Asia", "Africa", "North America", "Europe");
        addQuestion(set, 2, "What is the longest river in the world?", "Nile", "Amazon", "Mississippi", "Yangtze");
        addQuestion(set, 3, "What is the capital of France?", "Paris", "London", "Berlin", "Madrid");
        addQuestion(set, 4, "Which ocean is the largest?", "Pacific", "Atlantic", "Indian", "Arctic");
        addQuestion(set, 5, "What is the smallest country in the world?", "Vatican City", "Monaco", "San Marino", "Liechtenstein");
        addQuestion(set, 6, "On which continent is Egypt located?", "Africa", "Asia", "Europe", "South America");
        addQuestion(set, 7, "What is the capital of Japan?", "Tokyo", "Beijing", "Seoul", "Bangkok");
        addQuestion(set, 8, "What mountain range separates Europe and Asia?", "Ural Mountains", "Alps", "Himalayas", "Rocky Mountains");
        addQuestion(set, 9, "What is the largest desert in the world?", "Sahara", "Gobi", "Arabian", "Antarctic");
        addQuestion(set, 10, "How many continents are there?", "7", "5", "6", "8");
        addQuestion(set, 11, "What is the capital of Australia?", "Canberra", "Sydney", "Melbourne", "Brisbane");
        addQuestion(set, 12, "What is the highest mountain in the world?", "Mount Everest", "K2", "Kilimanjaro", "Denali");
        addQuestion(set, 13, "What country has the largest population?", "China", "India", "United States", "Indonesia");
        addQuestion(set, 14, "What is the capital of Brazil?", "Brasília", "Rio de Janeiro", "São Paulo", "Buenos Aires");
        addQuestion(set, 15, "What body of water separates Europe from Africa?", "Mediterranean Sea", "Atlantic Ocean", "Red Sea", "Black Sea");

        questionSetRepository.save(set);
    }

    private void createEnglishPreset() {
        QuestionSetEntity set = createSet("english-grammar", "English Grammar & Literature",
            "Grammar rules and literary terms",
            "English", "6-8");

        addQuestion(set, 1, "What is a noun?", "Person, place, thing, or idea", "Action word", "Describing word", "Connecting word");
        addQuestion(set, 2, "What is the plural of 'child'?", "Children", "Childs", "Childen", "Childrens");
        addQuestion(set, 3, "What is a metaphor?", "A comparison without using like or as", "A comparison using like or as", "An exaggeration", "A sound word");
        addQuestion(set, 4, "Which is correct: 'their', 'there', or 'they're' for possession?", "Their", "There", "They're", "All are correct");
        addQuestion(set, 5, "What is the past tense of 'run'?", "Ran", "Runned", "Running", "Runs");
        addQuestion(set, 6, "What is a simile?", "A comparison using like or as", "A comparison without using like or as", "An exaggeration", "A repeating sound");
        addQuestion(set, 7, "What is the subject in: 'The dog ran fast'?", "Dog", "Ran", "Fast", "The");
        addQuestion(set, 8, "What is an adjective?", "A word that describes a noun", "An action word", "A person, place, or thing", "A connecting word");
        addQuestion(set, 9, "What is personification?", "Giving human qualities to non-human things", "Extreme exaggeration", "A comparison", "Repeating sounds");
        addQuestion(set, 10, "What is the correct form: 'good' or 'well' in 'She plays piano ___'?", "Well", "Good", "Both", "Neither");
        addQuestion(set, 11, "What is alliteration?", "Repeating consonant sounds", "Repeating vowel sounds", "A comparison", "An exaggeration");
        addQuestion(set, 12, "What is a verb?", "An action word", "A describing word", "A person, place, or thing", "A connecting word");
        addQuestion(set, 13, "What is the protagonist of a story?", "The main character", "The villain", "The narrator", "The author");
        addQuestion(set, 14, "What is a synonym?", "A word with similar meaning", "A word with opposite meaning", "A word that sounds the same", "A describing word");
        addQuestion(set, 15, "What punctuation ends a question?", "Question mark", "Period", "Exclamation point", "Comma");

        questionSetRepository.save(set);
    }

    // ========== Helper Methods ==========

    private QuestionSetEntity createSet(String id, String name, String description,
                                          String subject, String gradeLevel) {
        QuestionSetEntity set = new QuestionSetEntity();
        set.setSetId(id);
        set.setName(name);
        set.setDescription(description);
        set.setSubject(subject);
        set.setGradeLevel(gradeLevel);
        set.setCreatedBy("SYSTEM");
        set.setIsPublic(true);
        return set;
    }

    private void addQuestion(QuestionSetEntity set, int order, String text,
                              String correct, String wrong1, String wrong2, String wrong3) {
        QuestionEntity q = new QuestionEntity();
        q.setQuestionId(set.getSetId() + "-q" + order);
        q.setQuestionSet(set);
        q.setQuestionText(text);
        q.setCorrectAnswer(correct);
        q.setWrongAnswer1(wrong1);
        q.setWrongAnswer2(wrong2);
        q.setWrongAnswer3(wrong3);
        q.setDifficulty(1);
        q.setOrderIndex(order);
        set.getQuestions().add(q);
    }
}

/*
 * Component of GAE Project for TMSCA Contest Automation
 * Copyright (C) 2013 Sushain Cherivirala
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see [http://www.gnu.org/licenses/].
 */

package contestTabulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import util.Pair;

public class School {
	final private String name;
	final private Level level;
	final private int lowGrade, highGrade;
	private final ArrayList<Student> students = new ArrayList<Student>();

	private final HashMap<Test, Integer> numTests = new HashMap<Test, Integer>();

	private final HashMap<Subject, Pair<Student[], Integer>> topScores = new HashMap<Subject, Pair<Student[], Integer>>();
	private final HashMap<Test, ArrayList<Score>> anonScores = new HashMap<Test, ArrayList<Score>>();
	private int totalScore;

	School(String name, Level level) {
		this.name = name;
		this.level = level;
		this.lowGrade = level.getLowGrade();
		this.highGrade = level.getHighGrade();
	}

	public ArrayList<Student> getStudents() {
		return students;
	}

	public HashMap<Test, ArrayList<Score>> getAnonScores() {
		return anonScores;
	}

	public ArrayList<Score> getAnonScores(Test test) {
		return anonScores.get(test);
	}

	public String getName() {
		return name;
	}

	public int getNumStudents() {
		return students.size();
	}

	public HashMap<Test, Integer> getNumTests() {
		return numTests;
	}

	public Level getLevel() {
		return level;
	}

	public Student[] getScoreStudents(Subject subject) {
		return topScores.get(subject).x;
	}

	public int getScore(Subject subject) {
		return topScores.get(subject).y;
	}

	public int getTotalScore() {
		return totalScore;
	}

	protected void addStudent(Student student) {
		students.add(student);
	}

	protected void addAnonScores(Test test, ArrayList<Score> scores) {
		anonScores.put(test, scores);
		if (!numTests.containsKey(test)) {
			numTests.put(test, scores.size());
		}
		else {
			numTests.put(test, numTests.get(test) + scores.size());
		}
	}

	public void calculateTestNums() {
		for (Student student : students) {
			int grade = student.getGrade();
			for (Subject s : student.getScores().keySet()) {
				Test test = Test.fromSubjectAndGrade(grade, s);
				if (!numTests.containsKey(test)) {
					numTests.put(test, 1);
				}
				else {
					numTests.put(test, numTests.get(test) + 1);
				}
			}

		}
	}

	private HashMap<Student, Score> calculateScore(final Subject subject) {
		ArrayList<Student> subjectStudents = new ArrayList<Student>();

		for (Student student : students) {
			if (student.hasScore(subject) && student.getScore(subject).getScoreNum() >= 0) {
				subjectStudents.add(student);
			}
		}

		for (int grade = lowGrade; grade <= highGrade; grade++) {
			ArrayList<Score> scores = anonScores.get(Test.valueOf(subject + Integer.toString(grade)));
			if (scores != null) {
				for (Score score : scores) {
					if (score.getScoreNum() > 0) {
						Student tempStudent = new Student(grade, this);
						tempStudent.setScore(subject, score);
						subjectStudents.add(tempStudent);
					}
				}
			}
		}

		Collections.sort(subjectStudents, Collections.reverseOrder(new Comparator<Student>() {
			@Override
			public int compare(Student s1, Student s2) {
				return s1.getScore(subject).compareTo(s2.getScore(subject));
			}
		}));

		int inHighGrade = 0;
		HashMap<Student, Score> top4 = new HashMap<Student, Score>();
		for (Student student : subjectStudents) {
			if (top4.size() < 4) {
				Score score = student.getScore(subject);
				if (highGrade == student.getGrade() && inHighGrade < 3) {
					top4.put(student, score);
					inHighGrade++;
				}
				else if (highGrade != student.getGrade()) {
					top4.put(student, score);
				}
			}
		}

		int totalScore = 0;
		for (Score score : top4.values()) {
			if (score != null) {
				totalScore += score.getScoreNum();
			}
		}
		topScores.put(subject, new Pair<Student[], Integer>(top4.keySet().toArray(new Student[top4.keySet().size()]), totalScore));

		return top4;
	}

	public void calculateScores() {
		for (Subject subject : Subject.getSubjects()) {
			calculateScore(subject);
		}
		if (level == Level.MIDDLE) {
			totalScore = topScores.get(Subject.N).y + topScores.get(Subject.C).y + (int) Math.round(topScores.get(Subject.M).y * 8.0 / 5.0 + topScores.get(Subject.S).y * 8.0 / 5.0);
		}
		else {
			totalScore = topScores.get(Subject.N).y + (int) Math.round(topScores.get(Subject.M).y * 10.0 / 9.0 + topScores.get(Subject.S).y * 10.0 / 9.0 + topScores.get(Subject.C).y * 8.0 / 7.0);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		School other = (School) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
			else if (!name.equals(other.name)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "School [name=" + name + ", level=" + level + ", totalScore=" + totalScore + "]";
	}
}

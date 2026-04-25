import { PgTable,uuid,text,timestamp, pgTable } from "drizzle-orm/pg-core";
import { practiceRecords, progress, userWordStats } from "./progress.model.js";
import { quizResults } from "./quiz.model.js";
import { relations } from "drizzle-orm";

export const users=pgTable("users",{
    id:uuid("id").defaultRandom().primaryKey(),
    username:text("username").notNull().unique(),
    email:text("email").notNull().unique(),
    password:text("password").notNull(),
    refreshToken:text("refresh_token"),
    createdAt:timestamp("created_at").defaultNow()
});

export const usersRelations = relations(users, ({ many, one }) => ({
  practiceRecords: many(practiceRecords),
  quizResults: many(quizResults),
  wordStats: many(userWordStats),
  progress: one(progress),
}));
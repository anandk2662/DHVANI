import { PgTable,uuid,text,timestamp, pgTable } from "drizzle-orm/pg-core";

export const users=pgTable("users",{
    id:uuid("id").defaultRandom().primaryKey(),
    username:text("username").notNull().unique(),
    email:text("email").notNull().unique(),
    password:text("password").notNull(),
    refreshToken:text("refresh_token"),
    createdAt:timestamp("created_at").defaultNow()
});
import "dotenv/config";
import {drizzle} from "drizzle-orm/node-postgres";
import {Pool} from "pg";

const databaseUrl = process.env.DATABASE_URL;

if (!databaseUrl) {
    throw new Error("DATABASE_URL is not set. Add it to your .env file.");
}

const pool=new Pool({
    connectionString:databaseUrl
})

export const connectDB=async()=>{
    try {
        const client=await pool.connect();
        console.log("connected to postgreSQL database successfully");
        client.release();
    } catch (error:any) {
        console.error("Database connection failed",error.message);
    }
}

export const db=drizzle(pool)
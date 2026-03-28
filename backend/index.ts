import express from "express";
import cors from "cors";
import { connectDB } from "./config/db.js";
import userRoutes from "./routes/user.routes.js";

const PORT=process.env.PORT||5000;
const app = express();

await connectDB();
app.use(cors());
app.use(express.json());
app.use("/api/users", userRoutes);

app.listen(PORT, () => console.log(`server running on port ${PORT} `))
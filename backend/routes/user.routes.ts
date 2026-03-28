import express from "express";
import {
	getCurrentUser,
	loginUser,
	logoutUser,
	refresh,
	registerUser,
} from "../controllers/users.controller.js";
import { authenticate } from "../middlewares/authMiddleware.js";

const router = express.Router();

router.post("/register", registerUser);
router.post("/login", loginUser);
router.post("/refresh", refresh);
router.post("/logout", logoutUser);
router.get("/me", authenticate, getCurrentUser);

export default router;
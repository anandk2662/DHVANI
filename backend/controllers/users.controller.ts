import type { Request, Response } from "express";
import { asyncHandler } from "../middlewares/asyncHandler.js";
import { users } from "../models/user.model.js";
import bcrypt from "bcryptjs";
import { generateAccessToken, generateRefreshToken } from "../utils/createToken.js";
import {
    getCurrentUserPayloadSchema,
    formatValidationError,
    loginUserSchema,
    logoutUserSchema,
    refreshTokenSchema,
    registerUserSchema
} from "../schemas/user.schema.js";
import { db } from "../config/db.js";
import { eq, or } from "drizzle-orm";

const registerUser=asyncHandler(async(req:Request,res:Response)=>{
    const parsedBody = registerUserSchema.safeParse(req.body);
    if (!parsedBody.success) {
        return res.status(400).json(formatValidationError(parsedBody.error));
    }

    const { username, email, password } = parsedBody.data;

    const existingUsers = await db
        .select()
        .from(users)
        .where(or(eq(users.email, email), eq(users.username, username)));

    const usernameTaken = existingUsers.some((user) => user.username === username);
    if (usernameTaken) {
        return res.status(400).json({ message: "Username already exists" });
    }

    const emailTaken = existingUsers.some((user) => user.email === email);
    if (emailTaken) {
        return res.status(400).json({ message: "Email already registered" });
    }

    const hashedPassword=await bcrypt.hash(password,10);

    const newUser = await db
        .insert(users)
        .values({
            username,
            email,
            password:hashedPassword
        })
        .returning();
    
    const { password: _, ...userData } = newUser[0]!;
    return res.status(201).json({data:userData,message:"user registered successfully"})
})

const loginUser=asyncHandler(async(req:Request,res:Response)=>{
    const parsedBody = loginUserSchema.safeParse(req.body);
    if (!parsedBody.success) {
        return res.status(400).json(formatValidationError(parsedBody.error));
    }

    const { identifier, password } = parsedBody.data;

    const [existingUser] = await db
        .select()
        .from(users)
        .where(or(eq(users.email, identifier), eq(users.username, identifier)));

    if(!existingUser){
        return res.status(404).json({message:"user not found . Please register"})
    }

    const passwordCorrect = await bcrypt.compare(password, existingUser.password);

    if(!passwordCorrect){
        return res.status(400).json("incorrect password");
    }

    const accessToken=generateAccessToken(existingUser.id);
    const refreshToken=generateRefreshToken(existingUser.id);

    await db
        .update(users)
        .set({ refreshToken })
        .where(eq(users.id, existingUser.id));

    return res.json({
        accessToken,
        refreshToken,
        user: {
            id: existingUser.id,
            username: existingUser.username,
            email: existingUser.email
        }
    })
})

const refresh=asyncHandler(async(req:Request,res:Response)=>{
    const parsedBody = refreshTokenSchema.safeParse(req.body);
    if (!parsedBody.success) {
        return res.status(400).json(formatValidationError(parsedBody.error));
    }

    const { refreshToken } = parsedBody.data;
    const [user] = await db
        .select()
        .from(users)
        .where(eq(users.refreshToken, refreshToken));

    if(!user){
        return res.status(403).json({message:"Invalid refresh token"});
    }

    const newAccessToken=generateAccessToken(user.id);
    res.json({accessToken:newAccessToken})
})


const logoutUser=asyncHandler(async(req:Request,res:Response)=>{
    const parsedBody = logoutUserSchema.safeParse(req.body);
    if (!parsedBody.success) {
        return res.status(400).json(formatValidationError(parsedBody.error));
    }

    const { refreshToken } = parsedBody.data;

    await db
        .update(users)
        .set({ refreshToken: null })
        .where(eq(users.refreshToken, refreshToken));

    res.json({message:"Logged out successfully"});
})

const getCurrentUser=asyncHandler(async(req:Request,res:Response)=>{
    if (!req.user) {
        return res.status(401).json({ message: "unauthorized" });
    }

    const parsedUser = getCurrentUserPayloadSchema.safeParse(req.user);
    if (!parsedUser.success) {
        return res.status(401).json(formatValidationError(parsedUser.error));
    }

    const { userId } = parsedUser.data;

    const [user] = await db
        .select({
            id: users.id,
            username: users.username,
            email: users.email,
            createdAt: users.createdAt
        })
        .from(users)
        .where(eq(users.id, userId));

    if (!user) {
        return res.status(404).json({ message: "user not found" });
    }

    return res.status(200).json({ data: user });

})

const updateCurrentUser=asyncHandler(async(req:Request,res:Response)=>{
    
})

export {registerUser,loginUser,logoutUser,refresh,getCurrentUser}
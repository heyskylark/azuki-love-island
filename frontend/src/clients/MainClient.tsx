import axios, { AxiosResponse } from "axios";
import Count from "../models/api/Count";
import ParticipantRequest from "../models/api/ParticipantRequest";
import ParticipantResponse, { SeasonParticipantsResponse } from "../models/api/ParticipantResponse";
import Season from "../models/api/Season";
import GenderedVoteBracket from "../models/api/GenderedVoteBracket";
import GenderedInitialBracket from "../models/api/GenderedInitialBracket";
import VoteRequest from "../models/api/VoteRequest";
import { GenderedVoteRoundsResponse } from "../models/api/GenderedRoundWinners";
import { CloudinarySignatureRequest, CloudinarySignatureResponse } from "../models/api/CloudinaryRequest";
import CloudinaryUploadResponse from "../models/api/CloudinaryUploadResponse";
import IslandTweets from "../models/api/IslandTweetsResponse";

const BASE_URL = process.env.REACT_APP_BASE_API_URL;
const CLOUDINARY_UPLOAD_URL = process.env.REACT_APP_CLOUDINARY_UPLOAD_URL;

export async function getLatestSeasonsTotalVoteResults(): Promise<AxiosResponse<GenderedVoteRoundsResponse>> {
    return axios.get(`${BASE_URL}/vote/totals/latest`);
}

export async function getSeasonsInitialBracket(): Promise<AxiosResponse<GenderedInitialBracket>> {
    return axios.get(`${BASE_URL}/brackets/latest`);
}

export async function voteOnGenderedBracket(body: VoteRequest): Promise<AxiosResponse<GenderedVoteBracket>> {
    return axios.post(`${BASE_URL}/vote`, body); 
}

export async function getLatestVoteBracket(): Promise<AxiosResponse<GenderedVoteBracket>> {
    return axios.get(`${BASE_URL}/vote/latest`);
}

export async function getLatestVoteBracketByTwitterHandle(twitterHandle: string): Promise<AxiosResponse<GenderedVoteBracket>> {
    return axios.get(`${BASE_URL}/vote/latest/users/${twitterHandle}`);
}

export async function getLatestSeason(): Promise<AxiosResponse<Season>> {
    return axios.get(`${BASE_URL}/seasons/latest`);
}

export async function getLatestSeasonSubmissionsCount(): Promise<AxiosResponse<Count>> {
    return axios.get(`${BASE_URL}/participants/seasons/latest/submissions/count`);
}

export async function submitParticipant(body: ParticipantRequest): Promise<AxiosResponse<ParticipantResponse>> {
    return axios.post(`${BASE_URL}/participants`, body);
}

export async function getSeasonParticipants(seasonNumber: number): Promise<AxiosResponse<SeasonParticipantsResponse>> {
    return axios.get(`${BASE_URL}/participants/seasons/${seasonNumber}`)
}

export async function getSeasonSubmissions(seasonNumber: number): Promise<AxiosResponse<SeasonParticipantsResponse>> {
    return axios.get(`${BASE_URL}/participants/seasons/${seasonNumber}/submissions`);
}

export async function getLatestSeasonParticipants(): Promise<AxiosResponse<SeasonParticipantsResponse>> {
    return axios.get(`${BASE_URL}/participants/seasons/latest`);
}

export async function fetchCloudinarySignature(body: CloudinarySignatureRequest): Promise<AxiosResponse<CloudinarySignatureResponse>> {
    return axios.post(`${BASE_URL}/images/signature`, body);
}

export async function uploadImageToCloudinary(body: FormData, transformations: string = ""): Promise<AxiosResponse<CloudinaryUploadResponse>> {
    const url = transformations.length > 0 ? `${CLOUDINARY_UPLOAD_URL}?transformation=${transformations}` : `${CLOUDINARY_UPLOAD_URL}`;

    return axios.post(url, body);
}

export async function getUsersLoveIslandTweets(twitterHandle: string): Promise<AxiosResponse<IslandTweets[]>> {
    return axios.get(`${BASE_URL}/twitter/island?handle=${twitterHandle}`)
}

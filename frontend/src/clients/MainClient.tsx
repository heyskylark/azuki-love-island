import axios, { AxiosResponse } from "axios";
import Count from "../models/api/Count";
import ParticipantRequest from "../models/api/ParticipantRequest";
import ParticipantResponse from "../models/api/ParticipantResponse";
import Season from "../models/Season";

const BASE_URL = process.env.REACT_APP_BASE_API_URL;

export async function getLatestSeason(): Promise<AxiosResponse<Season>> {
    return axios.get(`${BASE_URL}/seasons/latest`);
}

export async function getLatestSeasonParticipantCount(): Promise<AxiosResponse<Count>> {
    return axios.get(`${BASE_URL}/participants/seasons/latest/count`);
}

export async function submitParticipant(body: ParticipantRequest): Promise<AxiosResponse<ParticipantResponse>> {
    return axios.post(`${BASE_URL}/participants`, body);
}

export async function getLatestSeasonParticipants(): Promise<AxiosResponse<ParticipantResponse[]>> {
    return axios.get(`${BASE_URL}/participants/seasons/latest`);
}

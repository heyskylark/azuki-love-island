import axios, { AxiosResponse } from "axios";
import Count from "../models/api/Count";
import ParticipantRequest from "../models/api/ParticipantRequest";
import ParticipantResponse from "../models/api/ParticipantResponse";

const BASE_URL = process.env.REACT_APP_BASE_API_URL;

export async function getLatestSeasonParticipantCount(): Promise<AxiosResponse<Count>> {
    return axios.get(`${BASE_URL}/participants/seasons/latest/count`);
}

export async function submitParticipant(body: ParticipantRequest): Promise<AxiosResponse<ParticipantResponse>> {
    return axios.post(`${BASE_URL}/participants`, body);
}

export async function getLatestSeasonParticipants(): Promise<AxiosResponse<ParticipantResponse[]>> {
    return axios.get(`${BASE_URL}/participants/seasons/latest`);
}

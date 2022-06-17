import axios, { AxiosResponse } from "axios";
import Count from "../models/api/Count";
import ParticipantRequest from "../models/api/ParticipantRequest";
import ParticipantResponse from "../models/api/ParticipantResponse";
import Season from "../models/api/Season";
import GenderedVoteBracket from "../models/api/GenderedVoteBracket";
import GenderedInitialBracket from "../models/api/GenderedInitialBracket";
import VoteRequest from "../models/api/VoteRequest";

const BASE_URL = process.env.REACT_APP_BASE_API_URL;

export async function getSeasonsInitialBracket(): Promise<AxiosResponse<GenderedInitialBracket>> {
    return axios.get(`${BASE_URL}/brackets/latest`);
}

export async function voteOnGenderedBracket(body: VoteRequest): Promise<AxiosResponse<GenderedVoteBracket>> {
    return axios.post(`${BASE_URL}/vote`, body); 
}

export async function getLatestVoteBracket(): Promise<AxiosResponse<GenderedVoteBracket>> {
    return axios.get(`${BASE_URL}/vote/latest`);
}

export async function getLatestSeason(): Promise<AxiosResponse<Season>> {
    return axios.get(`${BASE_URL}/seasons/latest`);
}

export async function getLatestSeasonParticipantCount(): Promise<AxiosResponse<Count>> {
    return axios.get(`${BASE_URL}/participants/seasons/latest/count`);
}

export async function submitParticipant(body: ParticipantRequest): Promise<AxiosResponse<ParticipantResponse>> {
    return axios.post(`${BASE_URL}/participants`, body);
}

export async function getLatestSeasonParticipants(filter: string | null): Promise<AxiosResponse<ParticipantResponse[]>> {
    let url: string = ""
    if (filter) {
        url = `${BASE_URL}/participants/seasons/latest?filter=${filter}`
    } else {
        url = `${BASE_URL}/participants/seasons/latest`
    }

    return axios.get(url);
}

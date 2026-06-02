--
-- PostgreSQL database dump
--

\restrict V7Ns2asmbfVe7bcOKlKep39qteoumbtE84k4RWRCwJVQN0SBi9BQD0suNcFsgva

-- Dumped from database version 18.3 (Ubuntu 18.3-1.pgdg24.04+1)
-- Dumped by pg_dump version 18.3 (Ubuntu 18.3-1.pgdg24.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: events; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.events (
    id bigint NOT NULL,
    budget character varying(255) NOT NULL,
    currency character varying(255),
    draw_date character varying(255),
    name character varying(255) NOT NULL,
    organizer_email character varying(255),
    rules character varying(255),
    status character varying(255)
);


ALTER TABLE public.events OWNER TO postgres;

--
-- Name: events_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.events_id_seq OWNER TO postgres;

--
-- Name: events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.events_id_seq OWNED BY public.events.id;


--
-- Name: matches; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.matches (
    id bigint NOT NULL,
    avatar_color character varying(255),
    giver_id bigint NOT NULL,
    giver_name character varying(255) NOT NULL,
    receiver_id bigint NOT NULL,
    receiver_name character varying(255) NOT NULL,
    round integer,
    event_id bigint
);


ALTER TABLE public.matches OWNER TO postgres;

--
-- Name: matches_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.matches_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.matches_id_seq OWNER TO postgres;

--
-- Name: matches_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.matches_id_seq OWNED BY public.matches.id;


--
-- Name: participants; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.participants (
    id bigint NOT NULL,
    avatar_color character varying(255),
    email character varying(255) NOT NULL,
    has_spun boolean,
    name character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    wishlist_status character varying(255),
    created_at timestamp(6) without time zone,
    nickname character varying(255),
    event_id bigint,
    password_hash character varying(255),
    invite_token character varying(255),
    reset_password_expires_at timestamp(6) without time zone,
    reset_password_token character varying(255)
);


ALTER TABLE public.participants OWNER TO postgres;

--
-- Name: participants_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.participants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.participants_id_seq OWNER TO postgres;

--
-- Name: participants_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.participants_id_seq OWNED BY public.participants.id;


--
-- Name: events id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.events ALTER COLUMN id SET DEFAULT nextval('public.events_id_seq'::regclass);


--
-- Name: matches id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matches ALTER COLUMN id SET DEFAULT nextval('public.matches_id_seq'::regclass);


--
-- Name: participants id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.participants ALTER COLUMN id SET DEFAULT nextval('public.participants_id_seq'::regclass);


--
-- Data for Name: events; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.events (id, budget, currency, draw_date, name, organizer_email, rules, status) FROM stdin;
2	111111	KES	2026-05-20	tstttt	test		active
3	1500	KES	2024-12-20	Test Event	test@test.com	No gift cards	active
4	100000	KES	2026-05-28	TEST	TESTTEST@gmail.com		active
5	1500	KES	2024-12-20	Family Swap 2024	muenidoris22@gmail.com	No gift cards	active
6	1500	KES	2024-12-20	Family Swap 2024	muenidoris22@gmail.com	No gift cards	active
7	1500	KES	2024-12-20	Family Swap 2024	muenidoris22@gmail.com	No gift cards	active
8	1500	KES	2024-12-20	Family Swap 2024	muenidoris2w2@gmail.com	No gift cards	active
9	100000	KES	2026-05-21	TESER 002	Testing 002		active
10	2577	KES	2026-09-30	Santa	TESTER@gmail.com		active
11	2131	KES	2026-05-27	hjbbdjsbv	testtersdsaudjwsb@gmail.com		active
12	53261	KES	2026-06-04	jhbhjewasc	bjcb@gmail.com		active
13	53261	KES	2026-06-04	jhbhjewasc	bjcb@gmail.com	Yeeey...\n	active
1	1200	KES	2026-06-04	TEST	muenidoris04@gmail.com	Please Go Big for your partenr because someone will go big for you. Let's have fun	active
\.


--
-- Data for Name: matches; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.matches (id, avatar_color, giver_id, giver_name, receiver_id, receiver_name, round, event_id) FROM stdin;
1	#6a2a7a	1	Dad	5	Doris	1	1
3	#8a3a2a	3	Diana	6	Delvis	1	1
4	#c8453a	4	Dorah	1	Dad	1	1
5	#1a4a7a	5	Doris	4	Dorah	1	1
6	#2a7a3a	6	Delvis	2	Mum	1	1
2	#1a4a7a	2	Mum	4	Diana	1	1
7	#c8453a	12	hcgh	10	jhbhjewasc	1	12
\.


--
-- Data for Name: participants; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.participants (id, avatar_color, email, has_spun, name, status, wishlist_status, created_at, nickname, event_id, password_hash, invite_token, reset_password_expires_at, reset_password_token) FROM stdin;
1	#c8453a	muselabenjamin@yahoo.com	t	Dad	Joined	Submitted	\N	\N	1	$2a$10$h4F0G7OrIBDH9iYSU7d64uEIp2RnSuhJRwIYD9IutRKKZ97BKA5jK	\N	\N	\N
3	#7a5c1e	dianamatheka328@gmail.com	t	Diana	Joined	Submitted	\N	\N	1	$2a$10$h4F0G7OrIBDH9iYSU7d64uEIp2RnSuhJRwIYD9IutRKKZ97BKA5jK	\N	\N	\N
4	#1a4a7a	dorahmatheka@gmail.com	t	Dorah	Joined	Submitted	\N	\N	1	$2a$10$h4F0G7OrIBDH9iYSU7d64uEIp2RnSuhJRwIYD9IutRKKZ97BKA5jK	\N	\N	\N
7	#c8453a	Muenidoris04@gmail.com	f	Doris Test	Joined	Submitted	2026-05-14 10:17:10.149239	\N	9	$2a$10$h4F0G7OrIBDH9iYSU7d64uEIp2RnSuhJRwIYD9IutRKKZ97BKA5jK	\N	\N	\N
9	#c8453a	tester@gmail.com	f	Doris Tester	Joined	Pending	2026-05-23 10:40:34.764959	\N	10	\N	\N	\N	\N
10	#c8453a	bjcb@gmail.com	f	jhbhjewasc	Joined	Pending	2026-05-23 13:10:21.420969	\N	12	\N	\N	\N	\N
12	#c8453a	hvhhvyhvg@gmail.com	t	hcgh	Joined	Pending	2026-05-26 05:45:12.626365	\N	12	$2a$10$Up7Iz5rwWJXWmS11dqK8Mu5NoHBE1DQxdBxWUTg1RAmF2RbCFLne.	\N	\N	\N
5	#6a2a7a	muenidoris04@gmail.com	t	Doris	Joined	Submitted	\N	\N	1	$2a$10$GNhRw47LKA7MQJ6EAzHJjeAsuZC.UC3QPuPovBcQW5uZKSpQe.J4W	\N	\N	\N
8	#c8453a	test001@gmail.com	f	TESTER  001	Joined	Pending	2026-05-17 15:19:02.825388	\N	9	$2a$10$Oe1SDzf8o0SHJlSdjTDPpO4WTzFKftkn7IS.seHF7jr8MA71fbWHG	\N	\N	\N
6	#8a3a2a	mathekadelvis0@gmail.com	t	Delvis	Joined	Pending	\N	\N	1	$2a$10$h4F0G7OrIBDH9iYSU7d64uEIp2RnSuhJRwIYD9IutRKKZ97BKA5jK	\N	\N	\N
2	#2a7a3a	snowmbula@gmail.com	t	Mum	Joined	Pending	\N	\N	1	$2a$10$h4F0G7OrIBDH9iYSU7d64uEIp2RnSuhJRwIYD9IutRKKZ97BKA5jK	\N	\N	\N
\.


--
-- Name: events_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.events_id_seq', 13, true);


--
-- Name: matches_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.matches_id_seq', 7, true);


--
-- Name: participants_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.participants_id_seq', 12, true);


--
-- Name: events events_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.events
    ADD CONSTRAINT events_pkey PRIMARY KEY (id);


--
-- Name: matches matches_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matches
    ADD CONSTRAINT matches_pkey PRIMARY KEY (id);


--
-- Name: participants participants_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.participants
    ADD CONSTRAINT participants_pkey PRIMARY KEY (id);


--
-- Name: participants uk_2fhv3ysbhuar3qlb3g9i7wtro; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.participants
    ADD CONSTRAINT uk_2fhv3ysbhuar3qlb3g9i7wtro UNIQUE (reset_password_token);


--
-- Name: participants uk_5twl8ou1bu7oonhaubl6qeouh; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.participants
    ADD CONSTRAINT uk_5twl8ou1bu7oonhaubl6qeouh UNIQUE (email);


--
-- Name: participants uk_abmamwo5u9mtybcy25vgqhaxj; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.participants
    ADD CONSTRAINT uk_abmamwo5u9mtybcy25vgqhaxj UNIQUE (invite_token);


--
-- Name: matches uk_matches_giver_event_round; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matches
    ADD CONSTRAINT uk_matches_giver_event_round UNIQUE (giver_id, event_id, round);


--
-- Name: matches fk_match_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matches
    ADD CONSTRAINT fk_match_event FOREIGN KEY (event_id) REFERENCES public.events(id);


--
-- Name: participants fk_participant_event; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.participants
    ADD CONSTRAINT fk_participant_event FOREIGN KEY (event_id) REFERENCES public.events(id);


--
-- PostgreSQL database dump complete
--

\unrestrict V7Ns2asmbfVe7bcOKlKep39qteoumbtE84k4RWRCwJVQN0SBi9BQD0suNcFsgva

